package app.philab.common.presentation.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * PhiLab color roles. Built from one palette:
 * #122C34 · #0B4F6C · #2978A0 · #006476 · #4EA5D9 · #09D0CC · #6A9B9B
 */
@Immutable
data class LabColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryText: Color,
    val primarySoft: Color,
    val background: Color,
    val card: Color,
    val cardPressed: Color,
    val raised: Color,
    val highlight: Color,
    val onHighlight: Color,
    val onHighlightMuted: Color,
    val text: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val textDisabled: Color,
    val divider: Color,
    val secondary: Color,
    val tertiary: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val errorSoft: Color,
    val onErrorSoft: Color,
    val shadow: Color,
    val isDark: Boolean,
)

object PaletteTones {
    val Abyss = Color(0xFF122C34)
    val Deep = Color(0xFF0B4F6C)
    val Blue = Color(0xFF2978A0)
    val Teal = Color(0xFF006476)
    val Sky = Color(0xFF4EA5D9)
    val Cyan = Color(0xFF09D0CC)
    val Sage = Color(0xFF6A9B9B)

    val Night = Color(0xFF0E2329)
    val NightRaised = Color(0xFF183A44)
    val Mist = Color(0xFFEDF4F8)
    val SkyTint = Color(0xFFD7EBF6)
    val White = Color(0xFFFFFFFF)
    val Amber = Color(0xFFE2A400)
    val AmberLight = Color(0xFFF4C752)
    val Coral = Color(0xFFD64545)
    val CoralLight = Color(0xFFFF8A80)
}

val LightLabColors = LabColors(
    primary = PaletteTones.Blue,
    onPrimary = PaletteTones.White,
    primaryText = PaletteTones.Deep,
    primarySoft = PaletteTones.Sky.copy(alpha = 0.18f),
    background = PaletteTones.Mist,
    card = PaletteTones.White.copy(alpha = 0.92f),
    cardPressed = Color(0xFFDCE8EE),
    raised = PaletteTones.White,
    highlight = PaletteTones.SkyTint,
    onHighlight = PaletteTones.Abyss,
    onHighlightMuted = PaletteTones.Teal,
    text = PaletteTones.Abyss,
    textSecondary = Color(0xFF2C4A53),
    textMuted = Color(0xFF4F7778),
    textDisabled = PaletteTones.Sage.copy(alpha = 0.6f),
    divider = PaletteTones.Sage.copy(alpha = 0.28f),
    secondary = PaletteTones.Teal,
    tertiary = PaletteTones.Cyan,
    success = PaletteTones.Teal,
    warning = PaletteTones.Amber,
    error = PaletteTones.Coral,
    errorSoft = Color(0xFFFBE3E3),
    onErrorSoft = Color(0xFF8A1F1F),
    shadow = PaletteTones.Abyss,
    isDark = false,
)

val DarkLabColors = LabColors(
    primary = PaletteTones.Sky,
    onPrimary = PaletteTones.Abyss,
    primaryText = Color(0xFF8CC8EC),
    primarySoft = PaletteTones.Sky.copy(alpha = 0.18f),
    background = PaletteTones.Night,
    card = PaletteTones.Abyss.copy(alpha = 0.88f),
    cardPressed = PaletteTones.NightRaised,
    raised = PaletteTones.NightRaised,
    highlight = PaletteTones.Deep,
    onHighlight = PaletteTones.White,
    onHighlightMuted = Color(0xFFA5D3EC),
    text = PaletteTones.White,
    textSecondary = Color(0xFFC9DDE2),
    textMuted = PaletteTones.Sage,
    textDisabled = PaletteTones.Sage.copy(alpha = 0.5f),
    divider = PaletteTones.Sage.copy(alpha = 0.22f),
    secondary = PaletteTones.Cyan,
    tertiary = PaletteTones.Teal,
    success = PaletteTones.Cyan,
    warning = PaletteTones.AmberLight,
    error = PaletteTones.CoralLight,
    errorSoft = Color(0xFF4A1E22),
    onErrorSoft = Color(0xFFFFC2BC),
    shadow = Color(0xFF000000),
    isDark = true,
)

val LocalLabColors = staticCompositionLocalOf { LightLabColors }
