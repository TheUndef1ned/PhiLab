package app.philab.common.presentation.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import app.philab.common.presentation.components.effects.cardElevation
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

data class ToolItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit,
)

/** Grouped list of navigation cells inside one card. */
@Composable
fun ToolGroup(items: List<ToolItem>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LabSpacing.screen)
            .cardElevation(LabShapes.card)
            .clip(LabShapes.card)
            .background(LabTheme.colors.card)
    ) {
        items.forEachIndexed { index, item ->
            if (index != 0) {
                HorizontalDivider(
                    color = LabTheme.colors.divider,
                    thickness = LabDimens.divider,
                    modifier = Modifier.padding(start = 64.dp)
                )
            }
            ToolCell(item)
        }
    }
}

@Composable
private fun ToolCell(item: ToolItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = item.onClick)
            .padding(horizontal = LabSpacing.l, vertical = LabSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(LabSpacing.m)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(LabShapes.icon)
                .background(LabTheme.colors.primarySoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(item.icon, contentDescription = null, tint = LabTheme.colors.primary, modifier = Modifier.size(LabDimens.icon))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = item.title, style = LabTheme.type.body, color = LabTheme.colors.text)
            Text(text = item.subtitle, style = LabTheme.type.bodySmall, color = LabTheme.colors.textMuted)
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = LabTheme.colors.textMuted
        )
    }
}

