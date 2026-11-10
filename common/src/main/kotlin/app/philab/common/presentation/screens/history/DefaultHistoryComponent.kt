package app.philab.common.presentation.screens.history

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultHistoryComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    private val storeFactory: HistoryStoreFactory,
) : HistoryComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<HistoryStore.State> = store.stateFlow(scope)

    override fun onDelete(id: String) = store.accept(HistoryStore.Intent.Delete(id))
    override fun onClear() = store.accept(HistoryStore.Intent.Clear)
    override fun onFilter(kind: CalcKind?) = store.accept(HistoryStore.Intent.Filter(kind))

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
        ): DefaultHistoryComponent
    }
}
