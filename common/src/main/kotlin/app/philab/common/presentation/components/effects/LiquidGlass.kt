package app.philab.common.presentation.components.effects

import android.os.Build
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.Shadow
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme

/**
 * Liquid Glass surface (refraction at the edges, specular highlight, blur, vibrancy)
 * on top of content marked with `Modifier.layerBackdrop(backdrop)`.
 *
 * Lens refraction needs Android 13+ (AGSL), blur Android 12+; on Android 10–11 the
 * effects are skipped and the surface becomes an almost opaque fill instead.
 */
@Composable
fun Modifier.liquidGlass(
    backdrop: Backdrop,
    shape: CornerBasedShape = LabShapes.pill,
    tint: Color = LabTheme.colors.card,
    blurRadius: Dp = 4.dp,
    refractionHeight: Dp = 22.dp,
    refractionAmount: Dp = 26.dp,
    chromaticAberration: Boolean = false,
    highlight: Highlight? = Highlight.Default,
    shadow: Shadow? = Shadow.Default,
    exportedBackdrop: LayerBackdrop? = null,
    layerBlock: (GraphicsLayerScope.() -> Unit)? = null,
): Modifier {
    val surface = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        tint.copy(alpha = minOf(tint.alpha, if (LabTheme.colors.isDark) 0.45f else 0.35f))
    } else {
        tint.copy(alpha = 0.96f)
    }
    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            vibrancy()
            blur(blurRadius.toPx())
            lens(
                refractionHeight = refractionHeight.toPx(),
                refractionAmount = refractionAmount.toPx(),
                chromaticAberration = chromaticAberration,
            )
        },
        highlight = { highlight },
        shadow = { shadow },
        exportedBackdrop = exportedBackdrop,
        layerBlock = layerBlock,
        onDrawSurface = { drawRect(surface) },
    )
}
