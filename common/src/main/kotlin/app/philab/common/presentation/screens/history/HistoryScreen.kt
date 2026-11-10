package app.philab.common.presentation.screens.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(component: HistoryComponent) {
    val state by component.model.collectAsState()
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    val dateFormat = androidx.compose.runtime.remember { SimpleDateFormat("d MMM, HH:mm", Locale.forLanguageTag("ru")) }

    ScreenScaffold(
        insideTabs = true,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("История", calculationsLabel(state.records.size)) },
                rightActions = {
                    if (state.records.isNotEmpty()) {
                        IconButton(onClick = { confirmClear = true }) {
                            Icon(Icons.Outlined.DeleteSweep, contentDescription = "Очистить", tint = LabTheme.colors.error)
                        }
                    }
                }
            )
        }
    ) {
        if (state.records.isEmpty() && !state.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 120.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Пока пусто.\nСохраняйте расчёты кнопкой с закладкой.",
                        style = LabTheme.type.body,
                        color = LabTheme.colors.textMuted,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
        if (state.kinds.size > 1) {
            item(key = "filter") {
                ChipRow(
                    items = listOf<CalcKind?>(null) + state.kinds,
                    selected = state.filter,
                    label = { it?.title ?: "Все" },
                    onSelect = component::onFilter,
                    modifier = Modifier.padding(horizontal = LabSpacing.screen)
                )
            }
        }
        items(state.visible, key = { it.id }) { record ->
            SectionCard(
                title = record.title,
                actions = {
                    IconButton(onClick = { component.onDelete(record.id) }) {
                        Icon(Icons.Outlined.DeleteOutline, contentDescription = "Удалить", tint = LabTheme.colors.textMuted)
                    }
                }
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                    Text(record.kind.title, style = LabTheme.type.caption, color = LabTheme.colors.primaryText)
                    Text(dateFormat.format(Date(record.createdAt)), style = LabTheme.type.caption, color = LabTheme.colors.textMuted)
                }
                SelectionContainer {
                    Column(verticalArrangement = Arrangement.spacedBy(LabSpacing.xs)) {
                        Text(record.input, style = LabTheme.type.bodySmall, color = LabTheme.colors.textSecondary)
                        Text(record.result, style = LabTheme.type.bodyMedium, color = LabTheme.colors.text)
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Очистить историю?") },
            text = { Text("Все сохранённые расчёты будут удалены без возможности восстановления.") },
            confirmButton = {
                TextButton(onClick = { component.onClear(); confirmClear = false }) {
                    Text("Удалить", color = LabTheme.colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Отмена") }
            },
            containerColor = LabTheme.colors.raised,
        )
    }
}

private fun calculationsLabel(n: Int): String {
    val word = when {
        n % 100 in 11..14 -> "расчётов"
        n % 10 == 1 -> "расчёт"
        n % 10 in 2..4 -> "расчёта"
        else -> "расчётов"
    }
    return "$n $word"
}
