package app.philab.common.presentation.screens.block

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.block.BlockStore.Intent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultBlockComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onBack") private val onBack: () -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
    private val storeFactory: BlockStoreFactory,
) : BlockComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }
    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<BlockStore.State> = store.stateFlow(scope)

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is BlockStore.Label.ShowToast -> onMakeToast(label.text)
                }
            }
        }
    }

    override fun onBack() = onBack.invoke()
    override fun onLengthChange(text: String) = store.accept(Intent.ChangeLength(text))
    override fun onMaxErrorsChange(text: String) = store.accept(Intent.ChangeMaxErrors(text))
    override fun onManualP(manual: Boolean) = store.accept(Intent.SetManualP(manual))
    override fun onPChange(text: String) = store.accept(Intent.ChangeP(text))
    override fun onModulation(value: Modulation) = store.accept(Intent.SetModulation(value))
    override fun onCoherence(value: Coherence) = store.accept(Intent.SetCoherence(value))
    override fun onSnrChange(text: String) = store.accept(Intent.ChangeSnr(text))
    override fun onSnrUnit(unit: SnrUnit) = store.accept(Intent.SetSnrUnit(unit))
    override fun onTargetChange(text: String) = store.accept(Intent.ChangeTarget(text))
    override fun onSave() = store.accept(Intent.Save)

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onBack") onBack: () -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultBlockComponent
    }
}
