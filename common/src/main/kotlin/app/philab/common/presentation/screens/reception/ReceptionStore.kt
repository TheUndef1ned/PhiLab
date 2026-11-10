package app.philab.common.presentation.screens.reception

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.ReceptionResult
import app.philab.common.domain.reception.model.ThresholdMode
import app.philab.common.domain.reception.usecase.CalculateReceptionUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.domain.snr.Snr
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.reception.ReceptionStore.Intent
import app.philab.common.presentation.screens.reception.ReceptionStore.Label
import app.philab.common.presentation.screens.reception.ReceptionStore.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

enum class ThresholdKind(val title: String) {
    OPTIMAL("Оптимальный"),
    FIXED("γ = число"),
    FACTOR("γ = c·E/N₀"),
    NEYMAN_PEARSON("Нейман–Пирсон"),
}

interface ReceptionStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class SetCoherence(val value: Coherence) : Intent
        data class SetModulation(val value: Modulation) : Intent
        data class ChangeSnr(val text: String) : Intent
        data class SetSnrUnit(val unit: SnrUnit) : Intent
        data class ChangeP0(val text: String) : Intent
        data class SetThresholdKind(val kind: ThresholdKind) : Intent
        data class ChangeThreshold(val text: String) : Intent
        data class ChangePskDeviation(val text: String) : Intent
        data class SetFskWide(val wide: Boolean) : Intent
        data class ChangeFskDeltaT(val text: String) : Intent
        data class ChangeK1(val text: String) : Intent
        data class ChangeK0(val text: String) : Intent
        data class ChangeRho(val text: String) : Intent
        data object Save : Intent
    }

    data class State(
        val coherence: Coherence = Coherence.COHERENT,
        val modulation: Modulation = Modulation.AT,
        val snrText: String = "10",
        val snrUnit: SnrUnit = SnrUnit.TWO_E_N0,
        val p0Text: String = "0,5",
        val thresholdKind: ThresholdKind = ThresholdKind.OPTIMAL,
        val thresholdText: String = "",
        val pskDeviationText: String = "180",
        val fskWide: Boolean = true,
        val fskDeltaTText: String = "2π",
        val k1Text: String = "1",
        val k0Text: String = "4",
        val rhoText: String = "−1",
        val params: ReceptionParams? = null,
        val result: ReceptionResult? = null,
        val error: String? = null,
    ) {
        val availableThresholds: List<ThresholdKind>
            get() = when {
                coherence == Coherence.COHERENT -> ThresholdKind.entries
                modulation == Modulation.AT -> listOf(ThresholdKind.OPTIMAL, ThresholdKind.FIXED, ThresholdKind.NEYMAN_PEARSON)
                else -> listOf(ThresholdKind.OPTIMAL)
            }

        val availableModulations: List<Modulation>
            get() = if (coherence == Coherence.COHERENT) Modulation.entries else listOf(Modulation.AT, Modulation.FSK, Modulation.PSK)
    }

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class ReceptionStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val calculate: CalculateReceptionUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {

    fun create(): ReceptionStore =
        object : ReceptionStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ReceptionStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.Recalculate),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data object Recalculate : Action
    }

    private sealed interface Msg {
        data class Inputs(val state: State) : Msg
        data class Calculated(val params: ReceptionParams?, val result: ReceptionResult?, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        private var job: Job? = null

        override fun executeAction(action: Action) {
            recalculate(state())
        }

        override fun executeIntent(intent: Intent) {
            val s = state()
            val next = when (intent) {
                is Intent.SetCoherence -> s.copy(coherence = intent.value).normalized()
                is Intent.SetModulation -> s.copy(modulation = intent.value).normalized()
                is Intent.ChangeSnr -> s.copy(snrText = intent.text)
                is Intent.SetSnrUnit -> s.copy(snrUnit = intent.unit, snrText = convertSnr(s.snrText, s.snrUnit, intent.unit))
                is Intent.ChangeP0 -> s.copy(p0Text = intent.text)
                is Intent.SetThresholdKind -> s.copy(thresholdKind = intent.kind, thresholdText = defaultThresholdText(intent.kind))
                is Intent.ChangeThreshold -> s.copy(thresholdText = intent.text)
                is Intent.ChangePskDeviation -> s.copy(pskDeviationText = intent.text)
                is Intent.SetFskWide -> s.copy(fskWide = intent.wide)
                is Intent.ChangeFskDeltaT -> s.copy(fskDeltaTText = intent.text)
                is Intent.ChangeK1 -> s.copy(k1Text = intent.text)
                is Intent.ChangeK0 -> s.copy(k0Text = intent.text)
                is Intent.ChangeRho -> s.copy(rhoText = intent.text)
                Intent.Save -> {
                    save(s)
                    return
                }
            }
            dispatch(Msg.Inputs(next))
            recalculate(next)
        }

        private fun State.normalized(): State {
            val kinds = availableThresholds
            val modulation = if (modulation in availableModulations) modulation else Modulation.AT
            return copy(
                modulation = modulation,
                thresholdKind = if (thresholdKind in kinds) thresholdKind else ThresholdKind.OPTIMAL,
            )
        }

        private fun defaultThresholdText(kind: ThresholdKind): String = when (kind) {
            ThresholdKind.OPTIMAL -> ""
            ThresholdKind.FIXED -> "5"
            ThresholdKind.FACTOR -> "1/4"
            ThresholdKind.NEYMAN_PEARSON -> "10^-3"
        }

        private fun convertSnr(text: String, from: SnrUnit, to: SnrUnit): String {
            val v = ExpressionParser.parse(text) ?: return text
            if (from == SnrUnit.DB_TWO_E_N0 || v > 0) {
                return NumberFormatter.format(Snr.of(v, from).valueIn(to), 5)
            }
            return text
        }

        private fun recalculate(s: State) {
            job?.cancel()
            val params = try {
                buildParams(s)
            } catch (e: IllegalArgumentException) {
                dispatch(Msg.Calculated(null, null, e.message))
                return
            }
            job = scope.launch {
                val result = withContext(Dispatchers.Default) { calculate(params) }
                result
                    .onSuccess { dispatch(Msg.Calculated(params, it, null)) }
                    .onFailure { dispatch(Msg.Calculated(params, null, it.message ?: "Ошибка расчёта")) }
            }
        }

        private fun buildParams(s: State): ReceptionParams {
            val snrValue = num(s.snrText, "отношение сигнал/шум")
            val q = Snr.of(snrValue, s.snrUnit).q
            val p0 = num(s.p0Text, "p₀")
            require(p0 in 0.0..1.0) { "p₀ должно быть в [0; 1]" }
            val threshold = when (s.thresholdKind) {
                ThresholdKind.OPTIMAL -> ThresholdMode.Optimal
                ThresholdKind.FIXED -> ThresholdMode.Fixed(num(s.thresholdText, "γ"))
                ThresholdKind.FACTOR -> ThresholdMode.EnergyFactor(num(s.thresholdText, "c"))
                ThresholdKind.NEYMAN_PEARSON -> ThresholdMode.NeymanPearson(num(s.thresholdText, "α"))
            }
            return ReceptionParams(
                coherence = s.coherence,
                modulation = s.modulation,
                q = q,
                p0 = p0,
                threshold = threshold,
                pskDeviationDeg = if (s.modulation == Modulation.PSK) num(s.pskDeviationText, "Δφ") else 180.0,
                fskDeltaT = if (s.modulation == Modulation.FSK && !s.fskWide) num(s.fskDeltaTText, "ΔT") else null,
                customK1 = if (s.modulation == Modulation.CUSTOM) num(s.k1Text, "E₁/E") else 1.0,
                customK0 = if (s.modulation == Modulation.CUSTOM) num(s.k0Text, "E₀/E") else 0.0,
                customRho = if (s.modulation == Modulation.CUSTOM) num(s.rhoText, "ρ") else 0.0,
            )
        }

        private fun num(text: String, name: String): Double =
            ExpressionParser.parse(text) ?: throw IllegalArgumentException("Введите $name")

        private fun save(s: State) {
            val r = s.result ?: return
            val p = r.params
            val title = "${p.modulation.short}, ${p.coherence.title.lowercase()}, 2E/N₀ = ${NumberFormatter.format(p.q, 4)}"
            val input = buildString {
                append("p₀ = ${NumberFormatter.format(p.p0)}; ")
                append("порог: ${s.thresholdKind.title}")
                if (s.thresholdText.isNotBlank()) append(" (${s.thresholdText})")
            }
            val result = "Pe = ${NumberFormatter.probability(r.pe)}; α = ${NumberFormatter.probability(r.alpha)}; " +
                "β = ${NumberFormatter.probability(r.beta)}; γ = ${NumberFormatter.format(r.gamma, 5)}"
            scope.launch {
                saveRecord(CalcKind.RECEPTION, title, input, result)
                    .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
                    .onFailure { publish(Label.ShowToast("Не удалось сохранить")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> msg.state.copy(params = params, result = result, error = error)
            is Msg.Calculated -> copy(params = msg.params, result = msg.result, error = msg.error)
        }
    }
}
