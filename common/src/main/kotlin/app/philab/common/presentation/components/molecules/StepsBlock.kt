package app.philab.common.presentation.components.molecules

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

/** Collapsible "Решение по шагам" — the lines can be copied straight into the notebook. */
@Composable
fun StepsBlock(steps: List<String>, title: String = "Решение по шагам", initiallyExpanded: Boolean = true) {
    if (steps.isEmpty()) return
    var expanded by rememberSaveable { mutableStateOf(initiallyExpanded) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LabShapes.cardSmall)
            .background(LabTheme.colors.raised.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = LabSpacing.m, vertical = LabSpacing.s),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = LabTheme.type.label,
                color = LabTheme.colors.primaryText,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = LabTheme.colors.primary,
                modifier = Modifier.size(LabDimens.icon)
            )
        }
        AnimatedVisibility(visible = expanded) {
            SelectionContainer {
                Column(
                    modifier = Modifier.padding(start = LabSpacing.m, end = LabSpacing.m, bottom = LabSpacing.m),
                    verticalArrangement = Arrangement.spacedBy(LabSpacing.s)
                ) {
                    steps.forEachIndexed { i, step ->
                        Row {
                            Text(
                                text = "${i + 1}.",
                                style = LabTheme.type.bodySmall,
                                color = LabTheme.colors.textMuted,
                                modifier = Modifier.padding(end = LabSpacing.s)
                            )
                            Text(
                                text = step,
                                style = LabTheme.type.bodyMedium,
                                color = LabTheme.colors.text
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesBlock(notes: List<String>) {
    if (notes.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(LabSpacing.xs)) {
        notes.forEach { note ->
            Text(
                text = "• $note",
                style = LabTheme.type.bodySmall,
                color = LabTheme.colors.textSecondary
            )
        }
    }
}
