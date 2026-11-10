package app.philab.common.presentation.screens.snrsolver

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.reception.usecase.RequiredSnrUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.presentation.screens.snrsolver.RequiredSnrStore.Intent
import app.philab.common.presentation.screens.snrsolver.RequiredSnrStore.Label
import app.philab.common.presentation.screens.snrsolver.RequiredSnrStore.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface RequiredSnrStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ChangePe(val text: String) : Intent
        data class ChangePskDeviation(val text: String) : Intent
        data object Save : Intent
    }

    data class State(
        val peText: String = "10^-4",
        val pskDeviationText: String = "180",
        val isLoading: Boolean = false,
        val result: RequiredSnrUseCase.Result? = null,
        val error: String? = null,
    )

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class RequiredSnrStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val requiredSnr: RequiredSnrUseCase,
    private val saveRecord: SaveCalcRecordUseCase,
) {
    fun create(): RequiredSnrStore =
        object : RequiredSnrStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RequiredSnrStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Unit),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data class Inputs(val pe: String, val psk: String) : Msg
        data object Loading : Msg
        data class Done(val result: RequiredSnrUseCase.Result?, val error: String?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Unit, State, Msg, Label>() {
        private var job: Job? = null

        override fun executeAction(action: Unit) = compute(state())

        override fun executeIntent(intent: Intent) {
            val s = state()
            when (intent) {
                is Intent.ChangePe -> s.copy(peText = intent.text)
                is Intent.ChangePskDeviation -> s.copy(pskDeviationText = intent.text)
                Intent.Save -> {
                    save(s)
                    null
                }
            }?.let { next ->
                dispatch(Msg.Inputs(next.peText, next.pskDeviationText))
                compute(next)
            }
        }

        private fun compute(s: State) {
            job?.cancel()
            val pe = ExpressionParser.parse(s.peText)
            val dev = ExpressionParser.parse(s.pskDeviationText) ?: 180.0
            if (pe == null) {
                dispatch(Msg.Done(null, "Введите Pe"))
                return
            }
            dispatch(Msg.Loading)
            job = scope.launch {
                val r = withContext(Dispatchers.Default) { requiredSnr(pe, dev) }
                r.onSuccess { dispatch(Msg.Done(it, null)) }
                    .onFailure { dispatch(Msg.Done(null, it.message)) }
            }
        }

        private fun save(s: State) {
            val r = s.result ?: return
            val text = r.rows.joinToString("; ") { "${it.title}: ${NumberFormatter.format(it.q, 4)} (${NumberFormatter.fixed(it.db, 2)} дБ)" }
            scope.launch {
                saveRecord(CalcKind.REQUIRED_SNR, "Требуемое ОСШ для Pe = ${s.peText}", "Pe = ${s.peText}", text)
                    .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Inputs -> copy(peText = msg.pe, pskDeviationText = msg.psk)
            Msg.Loading -> copy(isLoading = true)
            is Msg.Done -> copy(result = msg.result, error = msg.error, isLoading = false)
        }
    }
}
