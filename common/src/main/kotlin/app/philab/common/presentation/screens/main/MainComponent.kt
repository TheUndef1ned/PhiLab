package app.philab.common.presentation.screens.main

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import app.philab.common.presentation.screens.history.HistoryComponent
import app.philab.common.presentation.screens.phi.PhiComponent
import app.philab.common.presentation.screens.reception.ReceptionComponent
import app.philab.common.presentation.screens.tools.ToolsComponent
import kotlinx.serialization.Serializable

interface MainComponent {

    val pages: Value<ChildPages<*, Child>>

    fun selectTab(tab: Tab)

    @Serializable
    enum class Tab {
        PHI,
        RECEPTION,
        TOOLS,
        HISTORY,
    }

    sealed interface Child {
        data class Phi(val component: PhiComponent) : Child
        data class Reception(val component: ReceptionComponent) : Child
        data class Tools(val component: ToolsComponent) : Child
        data class History(val component: HistoryComponent) : Child
    }
}
