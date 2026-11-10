package app.philab.common.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.philab.common.R

/** DM Sans (SIL Open Font License 1.1). */
val LabFontFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

private fun style(size: TextUnit, line: TextUnit, weight: FontWeight) = TextStyle(
    fontFamily = LabFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = line,
)

/** PhiLab text styles. */
@Immutable
object LabType {
    val h1 = style(30.sp, 38.sp, FontWeight.Bold)
    val h2 = style(21.sp, 26.sp, FontWeight.Bold)
    val title = style(17.sp, 22.sp, FontWeight.Bold)
    val body = style(16.sp, 22.sp, FontWeight.Normal)
    val bodyMedium = style(14.sp, 20.sp, FontWeight.Normal)
    val bodySmall = style(13.sp, 18.sp, FontWeight.Normal)
    val label = style(14.sp, 18.sp, FontWeight.Medium)
    val caption = style(12.sp, 16.sp, FontWeight.Medium)
    val hint = style(11.sp, 14.sp, FontWeight.Normal)
    val tab = style(10.sp, 12.sp, FontWeight.Bold)
}

/** Material3 fallback typography for stock widgets (dialogs etc.). */
val LabMaterialTypography = Typography(
    displayLarge = LabType.h1,
    displayMedium = LabType.h1,
    displaySmall = LabType.h2,
    headlineLarge = LabType.h2,
    headlineMedium = LabType.h2,
    headlineSmall = LabType.title,
    titleLarge = LabType.title,
    titleMedium = LabType.title,
    titleSmall = LabType.label,
    bodyLarge = LabType.body,
    bodyMedium = LabType.bodyMedium,
    bodySmall = LabType.bodySmall,
    labelLarge = LabType.label,
    labelMedium = LabType.caption,
    labelSmall = LabType.hint,
)
