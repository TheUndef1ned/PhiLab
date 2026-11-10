package app.philab.common.presentation.theme

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** Entry point to the PhiLab design system inside composables. */
object LabTheme {
    val colors: LabColors
        @Composable @ReadOnlyComposable
        get() = LocalLabColors.current

    val type: LabType
        get() = LabType
}

@Composable
fun PhiLabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkLabColors else LightLabColors

    val view = LocalView.current
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalLabColors provides colors) {
        MaterialTheme(
            colorScheme = colors.toMaterial(),
            typography = LabMaterialTypography,
            content = content,
        )
    }
}

/** Stock Material3 widgets (dialogs, progress bars) get the same colors. */
private fun LabColors.toMaterial(): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primarySoft,
        onPrimaryContainer = primaryText,
        secondary = secondary,
        tertiary = tertiary,
        background = background,
        onBackground = text,
        surface = raised,
        onSurface = text,
        surfaceVariant = card,
        onSurfaceVariant = textMuted,
        surfaceContainerHigh = raised,
        outline = divider,
        outlineVariant = divider,
        error = error,
        errorContainer = errorSoft,
        onErrorContainer = onErrorSoft,
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
