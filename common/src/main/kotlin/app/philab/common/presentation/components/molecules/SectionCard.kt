package app.philab.common.presentation.components.molecules

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.philab.common.presentation.components.effects.cardElevation
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

/** Rounded card with a soft shadow — the main content container of every screen. */
@Composable
fun SectionCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LabSpacing.screen)
            .cardElevation(LabShapes.card)
            .clip(LabShapes.card)
            .background(LabTheme.colors.card)
            .padding(LabSpacing.l),
        verticalArrangement = Arrangement.spacedBy(LabSpacing.m)
    ) {
        if (title != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = LabTheme.type.title,
                    color = LabTheme.colors.text,
                    modifier = Modifier.weight(1f)
                )
                actions()
            }
        }
        content()
    }
}
