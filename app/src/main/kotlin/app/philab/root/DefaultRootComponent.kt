package app.philab.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.DelicateDecomposeApi
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.presentation.screens.block.DefaultBlockComponent
import app.philab.common.presentation.screens.converter.DefaultConverterComponent
import app.philab.common.presentation.screens.dpsk.DefaultDpskComponent
import app.philab.common.presentation.screens.formulas.DefaultFormulasComponent
import app.philab.common.presentation.screens.main.DefaultMainComponent
import app.philab.common.presentation.screens.matchedfilter.DefaultMatchedFilterComponent
import app.philab.common.presentation.screens.phitable.DefaultPhiTableComponent
import app.philab.common.presentation.screens.plot.DefaultPlotComponent
import app.philab.common.presentation.screens.snrsolver.DefaultRequiredSnrComponent
import app.philab.common.utils.componentScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(DelicateDecomposeApi::class)
class DefaultRootComponent @AssistedInject constructor(
    private val mainComponentFactory: DefaultMainComponent.Factory,
    private val phiTableComponentFactory: DefaultPhiTableComponent.Factory,
    private val plotComponentFactory: DefaultPlotComponent.Factory,
    private val requiredSnrComponentFactory: DefaultRequiredSnrComponent.Factory,
    private val blockComponentFactory: DefaultBlockComponent.Factory,
    private val dpskComponentFactory: DefaultDpskComponent.Factory,
    private val matchedFilterComponentFactory: DefaultMatchedFilterComponent.Factory,
    private val converterComponentFactory: DefaultConverterComponent.Factory,
    private val formulasComponentFactory: DefaultFormulasComponent.Factory,
    @Assisted("componentContext") componentContext: ComponentContext,
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()
    private val scope = componentScope()

    private val _toast = MutableStateFlow<String?>(null)
    override val toast: StateFlow<String?> = _toast.asStateFlow()
    private var toastJob: Job? = null

    override val stack: Value<ChildStack<*, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Main,
            handleBackButton = true,
            childFactory = ::child
        )

    private fun child(config: Config, context: ComponentContext): RootComponent.Child = when (config) {
        Config.Main -> RootComponent.Child.Main(
            mainComponentFactory.create(
                componentContext = context,
                onOpenTable = { navigation.push(Config.PhiTable(it)) },
                onOpenPlot = { navigation.push(Config.Plot(it)) },
                onOpenRequiredSnr = { navigation.push(Config.RequiredSnr) },
                onOpenBlock = { navigation.push(Config.Block) },
                onOpenDpsk = { navigation.push(Config.Dpsk) },
                onOpenMatchedFilter = { navigation.push(Config.MatchedFilter) },
                onOpenConverter = { navigation.push(Config.Converter) },
                onOpenFormulas = { navigation.push(Config.Formulas) },
                onMakeToast = ::showToast,
            )
        )
        is Config.PhiTable -> RootComponent.Child.PhiTable(
            phiTableComponentFactory.create(
                componentContext = context,
                highlightIndex = config.highlightIndex,
                onBack = ::back,
            )
        )
        is Config.Plot -> RootComponent.Child.Plot(
            plotComponentFactory.create(componentContext = context, params = config.params, onBack = ::back)
        )
        Config.RequiredSnr -> RootComponent.Child.RequiredSnr(
            requiredSnrComponentFactory.create(context, onBack = ::back, onMakeToast = ::showToast)
        )
        Config.Block -> RootComponent.Child.Block(
            blockComponentFactory.create(context, onBack = ::back, onMakeToast = ::showToast)
        )
        Config.Dpsk -> RootComponent.Child.Dpsk(
            dpskComponentFactory.create(context, onBack = ::back, onMakeToast = ::showToast)
        )
        Config.MatchedFilter -> RootComponent.Child.MatchedFilter(
            matchedFilterComponentFactory.create(context, onBack = ::back, onMakeToast = ::showToast)
        )
        Config.Converter -> RootComponent.Child.Converter(
            converterComponentFactory.create(context, onBack = ::back, onMakeToast = ::showToast)
        )
        Config.Formulas -> RootComponent.Child.Formulas(
            formulasComponentFactory.create(context, onBack = ::back)
        )
    }

    private fun back() = navigation.pop()

    private fun showToast(text: String) {
        toastJob?.cancel()
        _toast.value = text
        toastJob = scope.launch {
            delay(TOAST_DURATION_MS)
            _toast.value = null
        }
    }

    @Serializable
    private sealed interface Config {
        @Serializable
        data object Main : Config

        @Serializable
        data class PhiTable(val highlightIndex: Int?) : Config

        @Serializable
        data class Plot(val params: ReceptionParams) : Config

        @Serializable
        data object RequiredSnr : Config

        @Serializable
        data object Block : Config

        @Serializable
        data object Dpsk : Config

        @Serializable
        data object MatchedFilter : Config

        @Serializable
        data object Converter : Config

        @Serializable
        data object Formulas : Config
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("componentContext") componentContext: ComponentContext): DefaultRootComponent
    }

    private companion object {
        const val TOAST_DURATION_MS = 1800L
    }
}
