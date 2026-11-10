package app.philab.common.presentation.components.atoms

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

/** Segmented pill selector: the selected option gets a soft primary fill and primary text. */
@Composable
fun <T> ChipRow(
    items: List<T>,
    selected: T?,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: (T) -> Boolean = { true },
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .clip(LabShapes.pill)
            .background(LabTheme.colors.card)
            .padding(LabSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(LabSpacing.xs)
    ) {
        items.forEach { item ->
            Chip(
                text = label(item),
                selected = item == selected,
                enabled = enabled(item),
                onClick = { onSelect(item) },
            )
        }
    }
}

@Composable
private fun Chip(text: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue = if (selected) LabTheme.colors.primarySoft else Color.Transparent,
        animationSpec = tween(200),
        label = "chipBg"
    )
    val fg by animateColorAsState(
        targetValue = when {
            !enabled -> LabTheme.colors.textDisabled
            selected -> LabTheme.colors.primary
            else -> LabTheme.colors.textSecondary
        },
        animationSpec = tween(200),
        label = "chipFg"
    )
    Box(
        modifier = Modifier
            .clip(LabShapes.pill)
            .background(bg)
            .clickable(enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = LabSpacing.m, vertical = LabSpacing.s)
    ) {
        Text(text = text, style = LabTheme.type.label, color = fg, maxLines = 1)
    }
}

