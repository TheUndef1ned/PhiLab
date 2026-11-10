package app.philab.common.presentation.screens.plot

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.SweepOutput
import app.philab.common.domain.reception.model.SweepVariable
import app.philab.common.presentation.screens.plot.PlotStore.Intent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultPlotComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("params") private val params: ReceptionParams,
    @Assisted("onBack") private val onBack: () -> Unit,
    private val storeFactory: PlotStoreFactory,
) : PlotComponent, ComponentContext by componentContext {

    private val store = instanceKeeper.getStore { storeFactory.create(params) }

    private val scope = componentScope()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val model: StateFlow<PlotStore.State> = store.stateFlow(scope)

    override fun onBack() = onBack.invoke()
    override fun onVariable(variable: SweepVariable) = store.accept(Intent.SetVariable(variable))
    override fun onOutput(output: SweepOutput) = store.accept(Intent.SetOutput(output))
    override fun onFromChange(text: String) = store.accept(Intent.ChangeFrom(text))
    override fun onToChange(text: String) = store.accept(Intent.ChangeTo(text))
    override fun onLogY(log: Boolean) = store.accept(Intent.SetLogY(log))
    override fun onCompare(compare: Boolean) = store.accept(Intent.SetCompare(compare))

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("params") params: ReceptionParams,
            @Assisted("onBack") onBack: () -> Unit,
        ): DefaultPlotComponent
    }
}
