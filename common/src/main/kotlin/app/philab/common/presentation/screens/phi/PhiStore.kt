package app.philab.common.presentation.screens.phi

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.normal.NormalProbabilityUseCase
import app.philab.common.domain.phi.model.InversePhiResult
import app.philab.common.domain.phi.model.PhiLookupResult
import app.philab.common.domain.phi.usecase.InversePhiUseCase
import app.philab.common.domain.phi.usecase.LookupPhiUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.presentation.screens.phi.PhiStore.Intent
import app.philab.common.presentation.screens.phi.PhiStore.Label
import app.philab.common.presentation.screens.phi.PhiStore.State
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PhiStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ChangeZ(val text: String) : Intent
        data class ChangeInverse(val text: String) : Intent
        data class ChangeNormalMean(val text: String) : Intent
        data class ChangeNormalVariance(val text: String) : Intent
        data class ChangeNormalA(val text: String) : Intent
        data class ChangeNormalB(val text: String) : Intent
        data object SaveLookup : Intent
        data object SaveInverse : Intent
        data object SaveNormal : Intent
    }

    data class State(
        val zText: String = "1,96",
        val lookup: PhiLookupResult? = null,
        val osipov: Double? = null,
        val zError: String? = null,
        val inverseText: String = "0,975",
        val inverse: InversePhiResult? = null,
        val inverseError: String? = null,
        val meanText: String = "3",
        val varianceText: String = "5",
        val aText: String = "5",
        val bText: String = "",
        val normal: NormalProbabilityUseCase.Result? = null,
        val normalError: String? = null,
    )

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class PhiStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val lookupPhi: LookupPhiUseCase,
    private val inversePhi: InversePhiUseCase,
    private val normalProbability: NormalProbabilityUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {

    fun create(): PhiStore =
        object : PhiStore, Store<Intent, State, Label> by storeFactory.create(
            name = "PhiStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.Init),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data object Init : Action
    }

    private sealed interface Msg {
        data class ZText(val text: String) : Msg
        data class Lookup(val result: PhiLookupResult?, val osipov: Double?, val error: String?) : Msg
        data class InverseText(val text: String) : Msg
        data class Inverse(val result: InversePhiResult?, val error: String?) : Msg
        data class NormalInputs(val mean: String, val variance: String, val a: String, val b: String) : Msg
        data class Normal(val result: NormalProbabilityUseCase.Result?, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        private var lookupJob: Job? = null
        private var inverseJob: Job? = null

        override fun executeAction(action: Action) {
            when (action) {
                Action.Init -> {
                    recomputeLookup(state().zText)
                    recomputeInverse(state().inverseText)
                    recomputeNormal(state())
                }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.ChangeZ -> {
                    dispatch(Msg.ZText(intent.text))
                    recomputeLookup(intent.text)
                }
                is Intent.ChangeInverse -> {
                    dispatch(Msg.InverseText(intent.text))
                    recomputeInverse(intent.text)
                }
                is Intent.ChangeNormalMean -> updateNormal(state().copy(meanText = intent.text))
                is Intent.ChangeNormalVariance -> updateNormal(state().copy(varianceText = intent.text))
                is Intent.ChangeNormalA -> updateNormal(state().copy(aText = intent.text))
                is Intent.ChangeNormalB -> updateNormal(state().copy(bText = intent.text))
                Intent.SaveLookup -> saveLookup()
                Intent.SaveInverse -> saveInverse()
                Intent.SaveNormal -> saveNormal()
            }
        }

        private fun recomputeLookup(text: String) {
            lookupJob?.cancel()
            val z = ExpressionParser.parse(text)
            if (z == null) {
                dispatch(Msg.Lookup(null, null, if (text.isBlank()) null else "Не удалось разобрать число"))
                return
            }
            lookupJob = scope.launch {
                lookupPhi(z)
                    .onSuccess { result ->
                        val osipov = if (z >= 0) NormalDistribution.osipovApproximation(z) else
                            1.0 - NormalDistribution.osipovApproximation(-z)
                        dispatch(Msg.Lookup(result, osipov, null))
                    }
                    .onFailure { dispatch(Msg.Lookup(null, null, it.message ?: "Ошибка чтения таблицы")) }
            }
        }

        private fun recomputeInverse(text: String) {
            inverseJob?.cancel()
            val p = ExpressionParser.parse(text)
            if (p == null) {
                dispatch(Msg.Inverse(null, if (text.isBlank()) null else "Не удалось разобрать число"))
                return
            }
            inverseJob = scope.launch {
                inversePhi(p)
                    .onSuccess { dispatch(Msg.Inverse(it, null)) }
                    .onFailure { dispatch(Msg.Inverse(null, it.message)) }
            }
        }

        private fun updateNormal(next: State) {
            dispatch(Msg.NormalInputs(next.meanText, next.varianceText, next.aText, next.bText))
            recomputeNormal(next)
        }

        private fun recomputeNormal(s: State) {
            val m = ExpressionParser.parse(s.meanText)
            val d = ExpressionParser.parse(s.varianceText)
            val a = ExpressionParser.parse(s.aText)
            val b = if (s.bText.isBlank()) null else ExpressionParser.parse(s.bText)
            if (m == null || d == null || a == null || (s.bText.isNotBlank() && b == null)) {
                dispatch(Msg.Normal(null, "Заполните m, D и порог a"))
                return
            }
            runCatching { normalProbability(NormalProbabilityUseCase.Params(m, d, a, b)) }
                .onSuccess { dispatch(Msg.Normal(it, null)) }
                .onFailure { dispatch(Msg.Normal(null, it.message)) }
        }

        private fun saveLookup() {
            val r = state().lookup ?: return
            save(
                CalcKind.PHI,
                "Φ(${NumberFormatter.format(r.z)})",
                "z = ${state().zText}",
                "Φ = ${NumberFormatter.format(r.phi, 8)} (${r.method.label})",
            )
        }

        private fun saveInverse() {
            val r = state().inverse ?: return
            save(
                CalcKind.INVERSE_PHI,
                "z по Φ = ${NumberFormatter.format(r.phi)}",
                "Φ = ${state().inverseText}",
                "z = ${NumberFormatter.format(r.z, 5)}",
            )
        }

        private fun saveNormal() {
            val s = state()
            val r = s.normal ?: return
            save(
                CalcKind.NORMAL,
                "N(${s.meanText}; ${s.varianceText}), a = ${s.aText}",
                "m = ${s.meanText}, D = ${s.varianceText}, a = ${s.aText}" + if (s.bText.isNotBlank()) ", b = ${s.bText}" else "",
                buildString {
                    append("P(X > a) = ${NumberFormatter.probability(r.greater)}; P(X < a) = ${NumberFormatter.probability(r.less)}")
                    r.between?.let { append("; P(a < X < b) = ${NumberFormatter.probability(it)}") }
                },
            )
        }

        private fun save(kind: CalcKind, title: String, input: String, result: String) {
            scope.launch {
                saveRecord(kind, title, input, result)
                    .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
                    .onFailure { publish(Label.ShowToast("Не удалось сохранить: ${it.message}")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.ZText -> copy(zText = msg.text)
            is Msg.Lookup -> copy(lookup = msg.result, osipov = msg.osipov, zError = msg.error)
            is Msg.InverseText -> copy(inverseText = msg.text)
            is Msg.Inverse -> copy(inverse = msg.result, inverseError = msg.error)
            is Msg.NormalInputs -> copy(meanText = msg.mean, varianceText = msg.variance, aText = msg.a, bText = msg.b)
            is Msg.Normal -> copy(normal = msg.result, normalError = msg.error)
        }
    }
}
