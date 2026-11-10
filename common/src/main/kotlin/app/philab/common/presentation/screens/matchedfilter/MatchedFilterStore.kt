package app.philab.common.presentation.screens.matchedfilter

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.matchedfilter.MatchedFilterUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.presentation.screens.matchedfilter.MatchedFilterStore.Intent
import app.philab.common.presentation.screens.matchedfilter.MatchedFilterStore.Label
import app.philab.common.presentation.screens.matchedfilter.MatchedFilterStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface MatchedFilterStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class SetMode(val mode: MatchedFilterUseCase.Mode) : Intent
        data class ChangeQ(val text: String) : Intent
        data class ChangeRatio(val text: String) : Intent
        data class ChangePhase(val text: String) : Intent
        data object Save : Intent
    }

    data class State(
        val mode: MatchedFilterUseCase.Mode = MatchedFilterUseCase.Mode.DURATION,
        val qText: String = "10",
        val ratioText: String = "0,5",
        val phaseText: String = "30",
        val result: MatchedFilterUseCase.Result? = null,
        val error: String? = null,
    )

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class MatchedFilterStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val useCase: MatchedFilterUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {
    fun create(): MatchedFilterStore =
        object : MatchedFilterStore, Store<Intent, State, Label> by storeFactory.create(
            name = "MatchedFilterStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data class Inputs(val state: State) : Msg
        data class Done(val result: MatchedFilterUseCase.Result?, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Unit, State, Msg, Label>() {

        override fun executeAction(action: Unit) = compute(state())

        override fun executeIntent(intent: Intent) {
            val s = state()
            val next = when (intent) {
                is Intent.SetMode -> s.copy(mode = intent.mode)
                is Intent.ChangeQ -> s.copy(qText = intent.text)
                is Intent.ChangeRatio -> s.copy(ratioText = intent.text)
                is Intent.ChangePhase -> s.copy(phaseText = intent.text)
                Intent.Save -> {
                    save(s)
                    return
                }
            }
            dispatch(Msg.Inputs(next))
            compute(next)
        }

        private fun compute(s: State) {
            val q = ExpressionParser.parse(s.qText)
            if (q == null) {
                dispatch(Msg.Done(null, "Введите 2E/N₀"))
                return
            }
            val result = when (s.mode) {
                MatchedFilterUseCase.Mode.DURATION -> {
                    val r = ExpressionParser.parse(s.ratioText) ?: return dispatch(Msg.Done(null, "Введите T/T₀"))
                    useCase.duration(q, r)
                }
                MatchedFilterUseCase.Mode.PHASE -> {
                    val phi = ExpressionParser.parse(s.phaseText) ?: return dispatch(Msg.Done(null, "Введите φ"))
                    useCase.phase(q, phi)
                }
            }
            result.onSuccess { dispatch(Msg.Done(it, null)) }.onFailure { dispatch(Msg.Done(null, it.message)) }
        }

        private fun save(s: State) {
            val r = s.result ?: return
            val input = when (s.mode) {
                MatchedFilterUseCase.Mode.DURATION -> "2E₀/N₀ = ${s.qText}, T/T₀ = ${s.ratioText}"
                MatchedFilterUseCase.Mode.PHASE -> "2E/N₀ = ${s.qText}, φ = ${s.phaseText}°"
            }
            scope.launch {
                saveRecord(
                    CalcKind.MATCHED_FILTER,
                    if (s.mode == MatchedFilterUseCase.Mode.DURATION) "СФ: рассогласование по T" else "Коррелятор: ошибка фазы",
                    input,
                    "q/q_max = ${NumberFormatter.format(r.gain, 5)}; q = ${NumberFormatter.format(r.qOut, 5)}",
                ).onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> msg.state.copy(result = result, error = error)
            is Msg.Done -> copy(result = msg.result, error = msg.error)
        }
    }
}
