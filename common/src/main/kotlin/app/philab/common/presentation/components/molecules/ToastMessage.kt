package app.philab.common.presentation.components.molecules

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.Backdrop
import app.philab.common.presentation.components.effects.liquidGlass
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabSpacing
import app.philab.common.presentation.theme.LabTheme

/** Liquid Glass toast over the current screen ([backdrop] = the screen layer). */
@Composable
fun ToastMessage(text: String, backdrop: Backdrop, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LabSpacing.l)
            .liquidGlass(backdrop = backdrop, shape = LabShapes.toast, refractionHeight = 14.dp, refractionAmount = 18.dp)
            .padding(horizontal = LabSpacing.l, vertical = LabSpacing.m)
    ) {
        Text(text = text, style = LabTheme.type.label, color = LabTheme.colors.text)
    }
}
