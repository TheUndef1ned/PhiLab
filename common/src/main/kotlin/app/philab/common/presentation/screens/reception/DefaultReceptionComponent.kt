package app.philab.common.presentation.screens.reception

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.reception.ReceptionStore.Intent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefaultReceptionComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onOpenPlot") private val onOpenPlot: (ReceptionParams) -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
    private val storeFactory: ReceptionStoreFactory,
) : ReceptionComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create() }

    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<ReceptionStore.State> = store.stateFlow(scope)

    init {
        scope.launch {
            store.labels.collect { label ->
                when (label) {
                    is ReceptionStore.Label.ShowToast -> onMakeToast(label.text)
                }
            }
        }
    }

    override fun onCoherence(value: Coherence) = store.accept(Intent.SetCoherence(value))
    override fun onModulation(value: Modulation) = store.accept(Intent.SetModulation(value))
    override fun onSnrChange(text: String) = store.accept(Intent.ChangeSnr(text))
    override fun onSnrUnit(unit: SnrUnit) = store.accept(Intent.SetSnrUnit(unit))
    override fun onP0Change(text: String) = store.accept(Intent.ChangeP0(text))
    override fun onThresholdKind(kind: ThresholdKind) = store.accept(Intent.SetThresholdKind(kind))
    override fun onThresholdChange(text: String) = store.accept(Intent.ChangeThreshold(text))
    override fun onPskDeviationChange(text: String) = store.accept(Intent.ChangePskDeviation(text))
    override fun onFskWide(wide: Boolean) = store.accept(Intent.SetFskWide(wide))
    override fun onFskDeltaTChange(text: String) = store.accept(Intent.ChangeFskDeltaT(text))
    override fun onK1Change(text: String) = store.accept(Intent.ChangeK1(text))
    override fun onK0Change(text: String) = store.accept(Intent.ChangeK0(text))
    override fun onRhoChange(text: String) = store.accept(Intent.ChangeRho(text))
    override fun onSave() = store.accept(Intent.Save)

    override fun onOpenPlot() {
        model.value.params?.let(onOpenPlot)
    }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onOpenPlot") onOpenPlot: (ReceptionParams) -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultReceptionComponent
    }
}
