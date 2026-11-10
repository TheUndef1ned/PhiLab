package app.philab.root

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import app.philab.common.presentation.components.molecules.ToastMessage
import app.philab.common.presentation.screens.block.BlockScreen
import app.philab.common.presentation.screens.converter.ConverterScreen
import app.philab.common.presentation.screens.dpsk.DpskScreen
import app.philab.common.presentation.screens.formulas.FormulasScreen
import app.philab.common.presentation.screens.main.MainContent
import app.philab.common.presentation.screens.matchedfilter.MatchedFilterScreen
import app.philab.common.presentation.screens.phitable.PhiTableScreen
import app.philab.common.presentation.screens.plot.PlotScreen
import app.philab.common.presentation.screens.snrsolver.RequiredSnrScreen
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import app.philab.common.presentation.theme.LabTheme

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    val toast by component.toast.collectAsState()
    val rootBackdrop = rememberLayerBackdrop()

    Box(modifier = modifier.fillMaxSize()) {
        Children(
            modifier = Modifier
                .fillMaxSize()
                .layerBackdrop(rootBackdrop)
                .background(LabTheme.colors.background),
            stack = component.stack,
            animation = stackAnimation(slide()),
        ) { child ->
            when (val instance = child.instance) {
                is RootComponent.Child.Main -> MainContent(instance.component)
                is RootComponent.Child.PhiTable -> PhiTableScreen(instance.component)
                is RootComponent.Child.Plot -> PlotScreen(instance.component)
                is RootComponent.Child.RequiredSnr -> RequiredSnrScreen(instance.component)
                is RootComponent.Child.Block -> BlockScreen(instance.component)
                is RootComponent.Child.Dpsk -> DpskScreen(instance.component)
                is RootComponent.Child.MatchedFilter -> MatchedFilterScreen(instance.component)
                is RootComponent.Child.Converter -> ConverterScreen(instance.component)
                is RootComponent.Child.Formulas -> FormulasScreen(instance.component)
            }
        }

        AnimatedVisibility(
            visible = toast != null,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 96.dp)
        ) {
            ToastMessage(text = toast.orEmpty(), backdrop = rootBackdrop)
        }
    }
}
