package app.philab.common.presentation.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun ResultRow(label: String, value: String, modifier: Modifier = Modifier, emphasized: Boolean = false) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = LabTheme.type.bodyMedium,
            color = LabTheme.colors.textMuted,
            modifier = Modifier.weight(1f)
        )
        SelectionContainer {
            Text(
                text = value,
                style = if (emphasized) LabTheme.type.h2 else LabTheme.type.title,
                color = if (emphasized) LabTheme.colors.primaryText else LabTheme.colors.text,
                textAlign = TextAlign.End,
            )
        }
    }
}

/** Big highlighted answer block. */
@Composable
fun AnswerBubble(label: String, value: String, caption: String? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(LabTheme.colors.highlight, LabShapes.answer)
            .padding(horizontal = LabSpacing.l, vertical = LabSpacing.m)
    ) {
        Text(text = label, style = LabTheme.type.caption, color = LabTheme.colors.onHighlightMuted)
        SelectionContainer {
            Text(text = value, style = LabTheme.type.h1, color = LabTheme.colors.onHighlight)
        }
        if (caption != null) {
            Text(text = caption, style = LabTheme.type.bodySmall, color = LabTheme.colors.onHighlightMuted)
        }
    }
}

@Composable
fun ErrorText(text: String) {
    Text(
        text = text,
        style = LabTheme.type.bodyMedium,
        color = LabTheme.colors.onErrorSoft,
        modifier = Modifier
            .fillMaxWidth()
            .background(LabTheme.colors.errorSoft, LabShapes.cardSmall)
            .padding(LabSpacing.m)
    )
}
