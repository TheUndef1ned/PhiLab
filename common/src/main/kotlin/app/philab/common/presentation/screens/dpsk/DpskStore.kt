package app.philab.common.presentation.screens.dpsk

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.dpsk.DpskCodecUseCase
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.presentation.screens.dpsk.DpskStore.Intent
import app.philab.common.presentation.screens.dpsk.DpskStore.Label
import app.philab.common.presentation.screens.dpsk.DpskStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class DpskMode(val title: String) { ENCODE("Кодировать"), DECODE("Декодировать") }

interface DpskStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class SetMode(val mode: DpskMode) : Intent
        data class ChangeSymbols(val text: String) : Intent
        data class ChangePhases(val text: String) : Intent
        data class SetReference(val phase: Int) : Intent
        data object Save : Intent
    }

    data class State(
        val mode: DpskMode = DpskMode.ENCODE,
        val symbolsText: String = "1 −1 −1 −1 1 1 −1 1 −1 1",
        val phasesText: String = "0 0 π π π 0 π 0 0 π π",
        val referencePhase: Int = 0,
        val output: String? = null,
        val outputSequence: List<String> = emptyList(),
        val steps: List<String> = emptyList(),
        val error: String? = null,
    )

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class DpskStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val codec: DpskCodecUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {
    fun create(): DpskStore =
        object : DpskStore, Store<Intent, State, Label> by storeFactory.create(
            name = "DpskStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data class Inputs(val state: State) : Msg
        data class Done(val output: String?, val sequence: List<String>, val steps: List<String>, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Unit, State, Msg, Label>() {

        override fun executeAction(action: Unit) = compute(state())

        override fun executeIntent(intent: Intent) {
            val s = state()
            val next = when (intent) {
                is Intent.SetMode -> s.copy(mode = intent.mode)
                is Intent.ChangeSymbols -> s.copy(symbolsText = intent.text)
                is Intent.ChangePhases -> s.copy(phasesText = intent.text)
                is Intent.SetReference -> s.copy(referencePhase = intent.phase)
                Intent.Save -> {
                    save(s)
                    return
                }
            }
            dispatch(Msg.Inputs(next))
            compute(next)
        }

        private fun compute(s: State) {
            when (s.mode) {
                DpskMode.ENCODE -> encode(s)
                DpskMode.DECODE -> decode(s)
            }
        }

        private fun encode(s: State) {
            val tokens = tokens(s.symbolsText)
            val bipolar = tokens.any { it == "-1" }
            val symbols = tokens.map { t ->
                when (t) {
                    "1", "+1" -> true
                    "0", "-1" -> false
                    else -> {
                        dispatch(Msg.Done(null, emptyList(), emptyList(), "Непонятный символ «$t»: используйте 1/0 или 1/−1"))
                        return
                    }
                }
            }
            val alphabet = if (bipolar) DpskCodecUseCase.Alphabet.BIPOLAR else DpskCodecUseCase.Alphabet.BINARY
            codec.encode(symbols, s.referencePhase, alphabet)
                .onSuccess { r ->
                    val seq = r.phases.map(DpskCodecUseCase::phaseLabel)
                    dispatch(Msg.Done(seq.joinToString(", "), seq, r.steps, null))
                }
                .onFailure { dispatch(Msg.Done(null, emptyList(), emptyList(), it.message)) }
        }

        private fun decode(s: State) {
            val phases = tokens(s.phasesText).map { t ->
                when (t) {
                    "0", "0°", "2π", "360" -> 0
                    "π", "pi", "180", "180°", "-π" -> 1
                    else -> {
                        dispatch(Msg.Done(null, emptyList(), emptyList(), "Непонятная фаза «$t»: используйте 0 и π"))
                        return
                    }
                }
            }
            codec.decode(phases)
                .onSuccess { r ->
                    val seq = r.symbols.map { if (it) "1" else "0" }
                    val bipolar = r.symbols.joinToString(", ") { if (it) "1" else "−1" }
                    dispatch(Msg.Done("${seq.joinToString(", ")}  (или $bipolar)", seq, r.steps, null))
                }
                .onFailure { dispatch(Msg.Done(null, emptyList(), emptyList(), it.message)) }
        }

        private fun tokens(text: String): List<String> = text
            .replace('−', '-')
            .replace('–', '-')
            .lowercase()
            .split(' ', ',', ';', '\n', '\t')
            .filter { it.isNotBlank() }

        private fun save(s: State) {
            val out = s.output ?: return
            val input = if (s.mode == DpskMode.ENCODE) s.symbolsText else s.phasesText
            scope.launch {
                saveRecord(CalcKind.DPSK, "ОФТ: ${s.mode.title.lowercase()}", input, out)
                    .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> msg.state.copy(output = output, outputSequence = outputSequence, steps = steps, error = error)
            is Msg.Done -> copy(output = msg.output, outputSequence = msg.sequence, steps = msg.steps, error = msg.error)
        }
    }
}
