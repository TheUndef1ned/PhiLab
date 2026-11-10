package app.philab.common.presentation.screens.plot

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.SweepOutput
import app.philab.common.domain.reception.model.SweepRequest
import app.philab.common.domain.reception.model.SweepResult
import app.philab.common.domain.reception.model.SweepVariable
import app.philab.common.domain.reception.usecase.BuildSweepUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.presentation.screens.plot.PlotStore.Intent
import app.philab.common.presentation.screens.plot.PlotStore.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface PlotStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class SetVariable(val variable: SweepVariable) : Intent
        data class SetOutput(val output: SweepOutput) : Intent
        data class ChangeFrom(val text: String) : Intent
        data class ChangeTo(val text: String) : Intent
        data class SetLogY(val log: Boolean) : Intent
        data class SetCompare(val compare: Boolean) : Intent
    }

    data class State(
        val base: ReceptionParams,
        val variable: SweepVariable = SweepVariable.Q,
        val output: SweepOutput = SweepOutput.PE,
        val fromText: String = "0",
        val toText: String = "30",
        val logY: Boolean = true,
        val compare: Boolean = true,
        val isLoading: Boolean = false,
        val result: SweepResult? = null,
        val error: String? = null,
    ) {
        val availableVariables: List<SweepVariable>
            get() = SweepVariable.entries.filter { v ->
                when (v) {
                    SweepVariable.Q, SweepVariable.Q_DB -> true
                    SweepVariable.GAMMA, SweepVariable.P0 ->
                        base.coherence == Coherence.COHERENT || base.modulation == Modulation.AT
                    SweepVariable.PSK_PHASE -> base.coherence == Coherence.COHERENT && base.modulation == Modulation.PSK
                    SweepVariable.FSK_DELTA_T -> base.coherence == Coherence.COHERENT && base.modulation == Modulation.FSK
                }
            }
    }
}

class PlotStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val buildSweep: BuildSweepUseCase,
) {

    fun create(base: ReceptionParams): PlotStore {
        val (from, to) = SweepRequest.defaultRange(SweepVariable.Q, base)
        return object : PlotStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "PlotStore",
            initialState = State(
                base = base,
                fromText = NumberFormatter.format(from),
                toText = NumberFormatter.format(to),
            ),
            bootstrapper = SimpleBootstrapper(Action.Build),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}
    }

    private sealed interface Action {
        data object Build : Action
    }

    private sealed interface Msg {
        data class Inputs(val state: State) : Msg
        data object Loading : Msg
        data class Built(val result: SweepResult?, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {

        private var job: Job? = null

        override fun executeAction(action: Action) = build(state())

        override fun executeIntent(intent: Intent) {
            val s = state()
            val next = when (intent) {
                is Intent.SetVariable -> {
                    val (from, to) = SweepRequest.defaultRange(intent.variable, s.base)
                    s.copy(
                        variable = intent.variable,
                        fromText = NumberFormatter.format(from, 5),
                        toText = NumberFormatter.format(to, 5),
                    )
                }
                is Intent.SetOutput -> s.copy(output = intent.output)
                is Intent.ChangeFrom -> s.copy(fromText = intent.text)
                is Intent.ChangeTo -> s.copy(toText = intent.text)
                is Intent.SetLogY -> s.copy(logY = intent.log)
                is Intent.SetCompare -> s.copy(compare = intent.compare)
            }
            dispatch(Msg.Inputs(next))
            if (intent !is Intent.SetLogY) build(next)
        }

        private fun build(s: State) {
            job?.cancel()
            val from = ExpressionParser.parse(s.fromText)
            val to = ExpressionParser.parse(s.toText)
            if (from == null || to == null) {
                dispatch(Msg.Built(null, "Введите диапазон"))
                return
            }
            dispatch(Msg.Loading)
            job = scope.launch {
                val request = SweepRequest(
                    base = s.base,
                    variable = s.variable,
                    from = from,
                    to = to,
                    points = if (s.base.coherence == Coherence.NONCOHERENT) 81 else 161,
                    output = s.output,
                    compareWithOptimal = s.compare,
                )
                val result = withContext(Dispatchers.Default) { buildSweep(request) }
                result
                    .onSuccess { dispatch(Msg.Built(it, null)) }
                    .onFailure { dispatch(Msg.Built(null, it.message ?: "Ошибка построения")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> msg.state.copy(result = result, error = error, isLoading = isLoading)
            Msg.Loading -> copy(isLoading = true)
            is Msg.Built -> copy(result = msg.result, error = msg.error, isLoading = false)
        }
    }
}
