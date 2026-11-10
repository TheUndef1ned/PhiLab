package app.philab.common.presentation.components.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import app.philab.common.presentation.theme.LabTheme

/** "Сохранить в историю" action for card headers. */
@Composable
fun SaveAction(enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(
            Icons.Outlined.BookmarkAdd,
            contentDescription = "В историю",
            tint = if (enabled) LabTheme.colors.primary else LabTheme.colors.textDisabled
        )
    }
}
