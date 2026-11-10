package app.philab.common.presentation.screens.phitable

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultPhiTableComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("highlightIndex") private val highlightIndex: Int?,
    @Assisted("onBack") private val onBack: () -> Unit,
    private val storeFactory: PhiTableStoreFactory,
) : PhiTableComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(highlightIndex) }

    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<PhiTableStore.State> = store.stateFlow(scope)

    override fun onBack() = onBack.invoke()

    override fun onSelect(index: Int) = store.accept(PhiTableStore.Intent.Select(index))

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("highlightIndex") highlightIndex: Int?,
            @Assisted("onBack") onBack: () -> Unit,
        ): DefaultPhiTableComponent
    }
}
