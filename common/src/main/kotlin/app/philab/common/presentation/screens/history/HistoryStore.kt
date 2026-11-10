package app.philab.common.presentation.screens.history

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.model.CalcRecord
import app.philab.common.domain.history.repository.HistoryRepository
import app.philab.common.presentation.screens.history.HistoryStore.Intent
import app.philab.common.presentation.screens.history.HistoryStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface HistoryStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class Delete(val id: String) : Intent
        data object Clear : Intent
        data class Filter(val kind: CalcKind?) : Intent
    }

    data class State(
        val isLoading: Boolean = true,
        val records: List<CalcRecord> = emptyList(),
        val filter: CalcKind? = null,
    ) {
        val visible: List<CalcRecord> get() = filter?.let { k -> records.filter { it.kind == k } } ?: records
        val kinds: List<CalcKind> get() = records.map { it.kind }.distinct()
    }
}

class HistoryStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val repository: HistoryRepository,
) {
    fun create(): HistoryStore =
        object : HistoryStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "HistoryStore",
            initialState = State(),
            bootstrapper = SimpleBootstrapper(Action.Observe),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data object Observe : Action
    }

    private sealed interface Msg {
        data class Records(val records: List<CalcRecord>) : Msg
        data class Filter(val kind: CalcKind?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {
        override fun executeAction(action: Action) {
            scope.launch {
                repository.observeAll().collect { dispatch(Msg.Records(it)) }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.Delete -> scope.launch { repository.delete(intent.id) }
                Intent.Clear -> scope.launch { repository.clear() }
                is Intent.Filter -> dispatch(Msg.Filter(intent.kind))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Records -> copy(
                isLoading = false,
                records = msg.records,
                filter = filter?.takeIf { k -> msg.records.any { it.kind == k } },
            )
            is Msg.Filter -> copy(filter = msg.kind)
        }
    }
}
