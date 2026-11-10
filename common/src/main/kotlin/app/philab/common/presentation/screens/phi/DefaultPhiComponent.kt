package app.philab.common.presentation.screens.phi

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultPhiComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onOpenTable") private val onOpenTable: (highlightIndex: Int?) -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
    private val phiStoreFactory: PhiStoreFactory,
) : PhiComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { phiStoreFactory.create() }

    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<PhiStore.State> = store.stateFlow(scope)

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is PhiStore.Label.ShowToast -> onMakeToast(label.text)
                }
            }
        }
    }

    override fun onZChange(text: String) = store.accept(PhiStore.Intent.ChangeZ(text))
    override fun onInverseChange(text: String) = store.accept(PhiStore.Intent.ChangeInverse(text))
    override fun onMeanChange(text: String) = store.accept(PhiStore.Intent.ChangeNormalMean(text))
    override fun onVarianceChange(text: String) = store.accept(PhiStore.Intent.ChangeNormalVariance(text))
    override fun onAChange(text: String) = store.accept(PhiStore.Intent.ChangeNormalA(text))
    override fun onBChange(text: String) = store.accept(PhiStore.Intent.ChangeNormalB(text))
    override fun onSaveLookup() = store.accept(PhiStore.Intent.SaveLookup)
    override fun onSaveInverse() = store.accept(PhiStore.Intent.SaveInverse)
    override fun onSaveNormal() = store.accept(PhiStore.Intent.SaveNormal)

    override fun onOpenTable() {
        onOpenTable.invoke(model.value.lookup?.cellIndex)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onOpenTable") onOpenTable: (highlightIndex: Int?) -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultPhiComponent
    }
}
