package app.philab.common.presentation.screens.main

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.ChildNavState.Status
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.router.pages.Pages
import com.arkivanov.decompose.router.pages.PagesNavigation
import com.arkivanov.decompose.router.pages.childPages
import com.arkivanov.decompose.router.pages.select
import com.arkivanov.decompose.value.Value
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.presentation.screens.history.DefaultHistoryComponent
import app.philab.common.presentation.screens.phi.DefaultPhiComponent
import app.philab.common.presentation.screens.reception.DefaultReceptionComponent
import app.philab.common.presentation.screens.tools.DefaultToolsComponent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory

/** Bottom tabs: Decompose childPages, one page per tab. */
class DefaultMainComponent @AssistedInject constructor(
    private val phiComponentFactory: DefaultPhiComponent.Factory,
    private val receptionComponentFactory: DefaultReceptionComponent.Factory,
    private val toolsComponentFactory: DefaultToolsComponent.Factory,
    private val historyComponentFactory: DefaultHistoryComponent.Factory,
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onOpenTable") private val onOpenTable: (highlightIndex: Int?) -> Unit,
    @Assisted("onOpenPlot") private val onOpenPlot: (ReceptionParams) -> Unit,
    @Assisted("onOpenRequiredSnr") private val onOpenRequiredSnr: () -> Unit,
    @Assisted("onOpenBlock") private val onOpenBlock: () -> Unit,
    @Assisted("onOpenDpsk") private val onOpenDpsk: () -> Unit,
    @Assisted("onOpenMatchedFilter") private val onOpenMatchedFilter: () -> Unit,
    @Assisted("onOpenConverter") private val onOpenConverter: () -> Unit,
    @Assisted("onOpenFormulas") private val onOpenFormulas: () -> Unit,
    @Assisted("onMakeToast") private val onMakeToast: (String) -> Unit,
) : MainComponent, ComponentContext by componentContext {

    private val navigation = PagesNavigation<MainComponent.Tab>()

    override val pages: Value<ChildPages<*, MainComponent.Child>> =
        childPages(
            source = navigation,
            serializer = MainComponent.Tab.serializer(),
            initialPages = { Pages(items = MainComponent.Tab.entries, selectedIndex = MainComponent.Tab.PHI.ordinal) },
            pageStatus = { index, pages ->
                if (index == pages.selectedIndex) Status.RESUMED else Status.CREATED
            },
            childFactory = ::child
        )

    override fun selectTab(tab: MainComponent.Tab) {
        navigation.select(tab.ordinal)
    }

    private fun child(tab: MainComponent.Tab, componentContext: ComponentContext): MainComponent.Child =
        when (tab) {
            MainComponent.Tab.PHI -> MainComponent.Child.Phi(
                phiComponentFactory.create(
                    componentContext = componentContext,
                    onOpenTable = onOpenTable,
                    onMakeToast = onMakeToast,
                )
            )
            MainComponent.Tab.RECEPTION -> MainComponent.Child.Reception(
                receptionComponentFactory.create(
                    componentContext = componentContext,
                    onOpenPlot = onOpenPlot,
                    onMakeToast = onMakeToast,
                )
            )
            MainComponent.Tab.TOOLS -> MainComponent.Child.Tools(
                toolsComponentFactory.create(
                    componentContext = componentContext,
                    onOpenRequiredSnr = onOpenRequiredSnr,
                    onOpenBlock = onOpenBlock,
                    onOpenDpsk = onOpenDpsk,
                    onOpenMatchedFilter = onOpenMatchedFilter,
                    onOpenConverter = onOpenConverter,
                    onOpenFormulas = onOpenFormulas,
                    onOpenTable = { onOpenTable(null) },
                )
            )
            MainComponent.Tab.HISTORY -> MainComponent.Child.History(
                historyComponentFactory.create(componentContext = componentContext)
            )
        }

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onOpenTable") onOpenTable: (highlightIndex: Int?) -> Unit,
            @Assisted("onOpenPlot") onOpenPlot: (ReceptionParams) -> Unit,
            @Assisted("onOpenRequiredSnr") onOpenRequiredSnr: () -> Unit,
            @Assisted("onOpenBlock") onOpenBlock: () -> Unit,
            @Assisted("onOpenDpsk") onOpenDpsk: () -> Unit,
            @Assisted("onOpenMatchedFilter") onOpenMatchedFilter: () -> Unit,
            @Assisted("onOpenConverter") onOpenConverter: () -> Unit,
            @Assisted("onOpenFormulas") onOpenFormulas: () -> Unit,
            @Assisted("onMakeToast") onMakeToast: (String) -> Unit,
        ): DefaultMainComponent
    }
}
