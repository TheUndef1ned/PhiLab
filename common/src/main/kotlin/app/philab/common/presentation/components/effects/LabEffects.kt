package app.philab.common.presentation.components.effects

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.utils.LocalHazeState
import dev.chrisbanes.haze.HazeProgressive
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

/** Real-time blur needs RenderEffect (Android 12+); older devices get a gradient fill. */
private val blurSupported: Boolean get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** Soft drop shadow plus a hairline border, for cards and floating bars. */
@Composable
fun Modifier.cardElevation(
    shape: Shape,
    elevation: Dp = LabDimens.cardElevation,
): Modifier {
    val shadowColor = LabTheme.colors.shadow.copy(alpha = if (LabTheme.colors.isDark) 0.5f else 0.10f)
    return this
        .shadow(elevation, shape, clip = false, ambientColor = shadowColor, spotColor = shadowColor)
        .border(LabDimens.divider, LabTheme.colors.divider, shape)
}

/** Top bar backdrop: blur that fades out towards the content. */
@Composable
fun Modifier.topBarBackdrop(): Modifier {
    val background = LabTheme.colors.background
    if (!blurSupported) {
        return background(Brush.verticalGradient(listOf(background, background.copy(alpha = 0.85f), Color.Transparent)))
    }
    return hazeEffect(
        state = LocalHazeState.current,
        style = HazeStyle(
            backgroundColor = background,
            tint = HazeTint(background.copy(alpha = 0.7f)),
            blurRadius = 24.dp,
        ),
    ) {
        progressive = HazeProgressive.verticalGradient(startIntensity = 1f, endIntensity = 0f)
    }
}
