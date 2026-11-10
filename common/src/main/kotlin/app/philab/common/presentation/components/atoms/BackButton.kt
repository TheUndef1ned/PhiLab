package app.philab.common.presentation.components.atoms

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabTheme

@Composable
fun BackButton(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBackIos,
            contentDescription = "Назад",
            tint = LabTheme.colors.primary,
            modifier = Modifier.size(LabDimens.icon)
        )
    }
}
