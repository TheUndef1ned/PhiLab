package app.philab.common.presentation.screens.main.views

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Functions
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.shadow.Shadow
import app.philab.common.presentation.components.effects.liquidGlass
import app.philab.common.presentation.screens.main.MainComponent
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabSpacing
import app.philab.common.presentation.theme.LabTheme
import kotlin.math.abs
import kotlin.math.roundToInt

private data class TabSpec(val tab: MainComponent.Tab, val icon: ImageVector, val label: String)

private val tabs = listOf(
    TabSpec(MainComponent.Tab.PHI, Icons.Rounded.Functions, "Φ(z)"),
    TabSpec(MainComponent.Tab.RECEPTION, Icons.AutoMirrored.Rounded.ShowChart, "Приём"),
    TabSpec(MainComponent.Tab.TOOLS, Icons.Rounded.Handyman, "Задачи"),
    TabSpec(MainComponent.Tab.HISTORY, Icons.Rounded.History, "История"),
)

/**
 * Floating Liquid Glass tab bar: the capsule refracts the screen behind it, and the selected tab
 * is marked by a glass lens (with chromatic aberration) that springs between tabs and
 * stretches while it moves.
 *
 * [backdrop] must be attached to the screen content with `Modifier.layerBackdrop`.
 */
@Composable
fun LabBottomBar(component: MainComponent, backdrop: Backdrop) {
    val pages by component.pages.subscribeAsState()
    val selected = pages.selectedIndex
    val barBackdrop = rememberLayerBackdrop()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = LabDimens.bottomBarMargin, vertical = LabSpacing.m)
            .height(LabDimens.bottomBarHeight)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .liquidGlass(backdrop = backdrop, exportedBackdrop = barBackdrop)
        )

        BoxWithConstraints(
            Modifier
                .fillMaxSize()
                .padding(LabSpacing.xs)
        ) {
            val tabWidth = maxWidth / tabs.size
            val tabWidthPx = with(LocalDensity.current) { tabWidth.toPx() }
            val position = remember { Animatable(selected.toFloat()) }
            LaunchedEffect(selected) {
                position.animateTo(selected.toFloat(), spring(dampingRatio = 0.68f, stiffness = 380f))
            }

            Box(
                Modifier
                    .offset { IntOffset((position.value * tabWidthPx).roundToInt(), 0) }
                    .width(tabWidth)
                    .fillMaxHeight()
                    .liquidGlass(
                        backdrop = rememberCombinedBackdrop(backdrop, barBackdrop),
                        tint = LabTheme.colors.primary.copy(alpha = 0.16f),
                        blurRadius = 0.dp,
                        refractionHeight = 10.dp,
                        refractionAmount = 16.dp,
                        chromaticAberration = true,
                        shadow = Shadow(radius = 10.dp, alpha = 0.6f),
                        layerBlock = {
                            // Liquid stretch while travelling between tabs
                            val travel = abs(position.value - position.targetValue).coerceAtMost(1f)
                            scaleX = 1f + 0.18f * travel
                            scaleY = 1f - 0.08f * travel
                        },
                    )
            )

            Row(Modifier.fillMaxSize()) {
                tabs.forEachIndexed { index, spec ->
                    BottomTab(
                        spec = spec,
                        selected = index == selected,
                        onClick = { component.selectTab(spec.tab) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTab(spec: TabSpec, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val color by animateColorAsState(
        targetValue = if (selected) LabTheme.colors.primary else LabTheme.colors.textSecondary,
        label = "tabColor",
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Icon(spec.icon, contentDescription = spec.label, tint = color, modifier = Modifier.size(LabDimens.bottomBarIcon))
        Text(spec.label, style = LabTheme.type.tab, color = color, maxLines = 1)
    }
}
