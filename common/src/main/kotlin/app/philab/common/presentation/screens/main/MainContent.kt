package app.philab.common.presentation.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import app.philab.common.presentation.screens.history.HistoryScreen
import app.philab.common.presentation.screens.main.views.LabBottomBar
import app.philab.common.presentation.screens.phi.PhiScreen
import app.philab.common.presentation.screens.reception.ReceptionScreen
import app.philab.common.presentation.screens.tools.ToolsScreen
import app.philab.common.presentation.theme.LabTheme

@Composable
fun MainContent(
    component: MainComponent,
    modifier: Modifier = Modifier
) {
    val pages by component.pages.subscribeAsState()
    // The current tab is recorded into this layer; the Liquid Glass tab bar refracts it.
    val screenBackdrop = rememberLayerBackdrop()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .layerBackdrop(screenBackdrop)
                .background(LabTheme.colors.background)
        ) {
            when (val child = pages.items[pages.selectedIndex].instance) {
                is MainComponent.Child.Phi -> PhiScreen(component = child.component)
                is MainComponent.Child.Reception -> ReceptionScreen(component = child.component)
                is MainComponent.Child.Tools -> ToolsScreen(component = child.component)
                is MainComponent.Child.History -> HistoryScreen(component = child.component)
                null -> Unit
            }
        }

        LabBottomBar(component = component, backdrop = screenBackdrop)
    }
}
