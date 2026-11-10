package app.philab.common.presentation.screens.dpsk

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.presentation.screens.dpsk.DpskStore.Intent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultDpskComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onBack") private val onBack: () -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
    private val storeFactory: DpskStoreFactory,
) : DpskComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<DpskStore.State> = store.stateFlow(scope)

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is DpskStore.Label.ShowToast -> onMakeToast(label.text)
                }
            }
        }
    }

    override fun onBack() = onBack.invoke()
    override fun onMode(mode: DpskMode) = store.accept(Intent.SetMode(mode))
    override fun onSymbolsChange(text: String) = store.accept(Intent.ChangeSymbols(text))
    override fun onPhasesChange(text: String) = store.accept(Intent.ChangePhases(text))
    override fun onReference(phase: Int) = store.accept(Intent.SetReference(phase))
    override fun onSave() = store.accept(Intent.Save)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onBack") onBack: () -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultDpskComponent
    }
}
