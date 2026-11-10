package app.philab.common.presentation.screens.block

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.block.BlockReceptionUseCase
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.usecase.CalculateReceptionUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.domain.snr.Snr
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.block.BlockStore.Intent
import app.philab.common.presentation.screens.block.BlockStore.Label
import app.philab.common.presentation.screens.block.BlockStore.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BlockStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ChangeLength(val text: String) : Intent
        data class ChangeMaxErrors(val text: String) : Intent
        data class SetManualP(val manual: Boolean) : Intent
        data class ChangeP(val text: String) : Intent
        data class SetModulation(val value: Modulation) : Intent
        data class SetCoherence(val value: Coherence) : Intent
        data class ChangeSnr(val text: String) : Intent
        data class SetSnrUnit(val unit: SnrUnit) : Intent
        data class ChangeTarget(val text: String) : Intent
        data object Save : Intent
    }

    data class State(
        val lengthText: String = "7",
        val maxErrorsText: String = "0",
        val manualP: Boolean = false,
        val pText: String = "0,01",
        val modulation: Modulation = Modulation.AT,
        val coherence: Coherence = Coherence.NONCOHERENT,
        val snrText: String = "4",
        val snrUnit: SnrUnit = SnrUnit.TWO_E_N0,
        val targetText: String = "0,99",
        val symbolP: Double? = null,
        val symbolSteps: List<String> = emptyList(),
        val result: BlockReceptionUseCase.Result? = null,
        val error: String? = null,
    )

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class BlockStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val block: BlockReceptionUseCase,
    private val reception: CalculateReceptionUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {
    fun create(): BlockStore =
        object : BlockStore, Store<Intent, State, Label> by storeFactory.create(
            name = "BlockStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data class Inputs(val state: State) : Msg
        data class Done(
            val symbolP: Double?,
            val symbolSteps: List<String>,
            val result: BlockReceptionUseCase.Result?,
            val error: String?,
        ) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Unit, State, Msg, Label>() {
        private var job: Job? = null

        override fun executeAction(action: Unit) = compute(state())

        override fun executeIntent(intent: Intent) {
            val s = state()
            val next = when (intent) {
                is Intent.ChangeLength -> s.copy(lengthText = intent.text)
                is Intent.ChangeMaxErrors -> s.copy(maxErrorsText = intent.text)
                is Intent.SetManualP -> s.copy(manualP = intent.manual)
                is Intent.ChangeP -> s.copy(pText = intent.text)
                is Intent.SetModulation -> s.copy(
                    modulation = intent.value,
                    coherence = if (intent.value == Modulation.PSK) Coherence.COHERENT else s.coherence,
                )
                is Intent.SetCoherence -> s.copy(
                    coherence = intent.value,
                    modulation = if (intent.value == Coherence.NONCOHERENT && s.modulation == Modulation.PSK) Modulation.FSK else s.modulation,
                )
                is Intent.ChangeSnr -> s.copy(snrText = intent.text)
                is Intent.SetSnrUnit -> s.copy(snrUnit = intent.unit)
                is Intent.ChangeTarget -> s.copy(targetText = intent.text)
                Intent.Save -> {
                    save(s)
                    return
                }
            }
            dispatch(Msg.Inputs(next))
            compute(next)
        }

        private fun compute(s: State) {
            job?.cancel()
            val n = ExpressionParser.parse(s.lengthText)?.toInt()
            val k = ExpressionParser.parse(s.maxErrorsText)?.toInt()
            val target = if (s.targetText.isBlank()) null else ExpressionParser.parse(s.targetText)
            if (n == null || k == null) {
                dispatch(Msg.Done(null, emptyList(), null, "Введите N и k"))
                return
            }
            job = scope.launch {
                val outcome = withContext(Dispatchers.Default) {
                    runCatching {
                        val (p, steps) = symbolProbability(s)
                        Triple(p, steps, block(BlockReceptionUseCase.Params(n, k, p, target)).getOrThrow())
                    }
                }
                outcome
                    .onSuccess { (p, steps, r) -> dispatch(Msg.Done(p, steps, r, null)) }
                    .onFailure { dispatch(Msg.Done(null, emptyList(), null, it.message)) }
            }
        }

        private fun symbolProbability(s: State): Pair<Double, List<String>> {
            if (s.manualP) {
                val p = ExpressionParser.parse(s.pText) ?: throw IllegalArgumentException("Введите p")
                return p to listOf("p задана вручную: ${NumberFormatter.probability(p)}")
            }
            val v = ExpressionParser.parse(s.snrText) ?: throw IllegalArgumentException("Введите ОСШ")
            val q = Snr.of(v, s.snrUnit).q
            val r = reception(
                ReceptionParams(coherence = s.coherence, modulation = s.modulation, q = q, p0 = 0.5)
            ).getOrThrow()
            return r.pe to listOf("Вероятность ошибки на символ (${s.modulation.short}, ${s.coherence.title.lowercase()}):") + r.steps
        }

        private fun save(s: State) {
            val r = s.result ?: return
            val input = "N = ${s.lengthText}, ошибок ≤ ${s.maxErrorsText}, p = ${s.symbolP?.let(NumberFormatter::probability)}"
            val result = buildString {
                append("P = ${NumberFormatter.probability(r.pCorrect)}")
                r.repetitions?.let { append("; повторов: $it") }
                append("; по шумам: ${NumberFormatter.probability(r.falseTriggerHalf)}")
            }
            scope.launch {
                saveRecord(CalcKind.BLOCK, "Слово из ${s.lengthText} элементов", input, result)
                    .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> msg.state.copy(symbolP = symbolP, symbolSteps = symbolSteps, result = result, error = error)
            is Msg.Done -> copy(symbolP = msg.symbolP, symbolSteps = msg.symbolSteps, result = msg.result, error = msg.error)
        }
    }
}
