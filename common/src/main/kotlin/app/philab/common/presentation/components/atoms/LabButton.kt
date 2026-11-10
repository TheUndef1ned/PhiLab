package app.philab.common.presentation.components.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

enum class LabButtonStyle { Primary, Secondary }

@Composable
fun LabButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: LabButtonStyle = LabButtonStyle.Primary,
    enabled: Boolean = true,
) {
    val bg = when {
        !enabled -> LabTheme.colors.cardPressed
        style == LabButtonStyle.Primary -> LabTheme.colors.primary
        else -> LabTheme.colors.primarySoft
    }
    val fg = when {
        !enabled -> LabTheme.colors.textDisabled
        style == LabButtonStyle.Primary -> LabTheme.colors.onPrimary
        else -> LabTheme.colors.primary
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(LabDimens.fieldHeight)
            .clip(LabShapes.button)
            .background(bg)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = LabSpacing.l),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LabSpacing.s, Alignment.CenterHorizontally)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(LabDimens.icon))
        }
        Text(text = text, style = LabTheme.type.title, color = fg)
    }
}
