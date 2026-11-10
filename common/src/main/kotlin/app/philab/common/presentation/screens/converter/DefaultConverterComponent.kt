package app.philab.common.presentation.screens.converter

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.converter.ConverterStore.Intent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultConverterComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onBack") private val onBack: () -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
    private val storeFactory: ConverterStoreFactory,
) : ConverterComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ConverterStore.State> = store.stateFlow(scope)

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is ConverterStore.Label.ShowToast -> onMakeToast(label.text)
                }
            }
        }
    }

    override fun onBack() = onBack.invoke()
    override fun onValueChange(text: String) = store.accept(Intent.ChangeValue(text))
    override fun onUnit(unit: SnrUnit) = store.accept(Intent.SetUnit(unit))
    override fun onSave() = store.accept(Intent.Save)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onBack") onBack: () -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultConverterComponent
    }
}
