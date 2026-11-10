package app.philab.common.presentation.screens.phitable

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.SimpleBootstrapper
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.phi.model.PhiTable
import app.philab.common.domain.phi.repository.PhiTableRepository
import app.philab.common.presentation.screens.phitable.PhiTableStore.Intent
import app.philab.common.presentation.screens.phitable.PhiTableStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface PhiTableStore : Store<Intent, State, Nothing> {

    sealed interface Intent {
        data class Select(val index: Int?) : Intent
    }

    data class State(
        val table: PhiTable? = null,
        val selectedIndex: Int? = null,
        val error: String? = null,
    )
}

class PhiTableStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val repository: PhiTableRepository,
) {

    fun create(highlightIndex: Int?): PhiTableStore =
        object : PhiTableStore, Store<Intent, State, Nothing> by storeFactory.create(
            name = "PhiTableStore",
            initialState = State(selectedIndex = highlightIndex),
            bootstrapper = SimpleBootstrapper(Action.Load),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Action {
        data object Load : Action
    }

    private sealed interface Msg {
        data class Loaded(val table: PhiTable) : Msg
        data class Failed(val error: String) : Msg
        data class Selected(val index: Int?) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Action, State, Msg, Nothing>() {
        override fun executeAction(action: Action) {
            scope.launch {
                repository.getTable()
                    .onSuccess { dispatch(Msg.Loaded(it)) }
                    .onFailure { dispatch(Msg.Failed(it.message ?: "Не удалось загрузить таблицу")) }
            }
        }

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.Select -> dispatch(Msg.Selected(intent.index))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Loaded -> copy(table = msg.table, error = null)
            is Msg.Failed -> copy(error = msg.error)
            is Msg.Selected -> copy(selectedIndex = msg.index)
        }
    }
}
