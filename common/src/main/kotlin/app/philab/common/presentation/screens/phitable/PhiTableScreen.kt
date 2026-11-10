package app.philab.common.presentation.screens.phitable

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import app.philab.common.domain.phi.model.PhiTable
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.molecules.AnswerBubble
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

private val Z_COLUMN_WIDTH = 52.dp

@Composable
fun PhiTableScreen(component: PhiTableComponent) {
    val state by component.model.collectAsState()
    val hScroll = rememberScrollState()
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    LaunchedEffect(state.table, state.selectedIndex) {
        val table = state.table ?: return@LaunchedEffect
        val index = state.selectedIndex ?: return@LaunchedEffect
        val col = index % table.columns
        val px = with(density) { (LabDimens.tableCellWidth * col).toPx() }.toInt()
        hScroll.animateScrollTo((px - 120).coerceAtLeast(0))
        // items: selected card, spacer, header, rows…
        listState.animateScrollToItem((index / table.columns + 3 - 4).coerceAtLeast(0))
    }

    ScreenScaffold(
        insideTabs = false,
        listState = listState,
        itemSpacing = 0.dp,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Таблица Φ(z)", "Приложение IV · Hald, 1952") },
                leftActions = { BackButton(component::onBack) }
            )
        }
    ) {
        state.error?.let { err -> item { SectionCard { ErrorText(err) } } }
        val table = state.table ?: return@ScreenScaffold

        item(key = "selected") {
            SectionCard {
                val idx = state.selectedIndex
                if (idx != null && idx in 0 until table.size) {
                    AnswerBubble(
                        label = "Φ(${NumberFormatter.fixed(table.zAt(idx), 2)})",
                        value = NumberFormatter.tableNotation(table.valueAt(idx), table.decimalsAt(idx)),
                        caption = "= ${NumberFormatter.format(table.valueAt(idx), 10)}",
                    )
                } else {
                    Text(
                        "Нажмите на ячейку, чтобы увидеть значение полностью",
                        style = LabTheme.type.bodyMedium,
                        color = LabTheme.colors.textMuted
                    )
                }
                Text(
                    "Φ(−z) = 1 − Φ(z). Запись 0,9³8215 = 0,9998215 (индекс — число девяток).",
                    style = LabTheme.type.bodySmall,
                    color = LabTheme.colors.textMuted
                )
            }
        }

        item(key = "gap") { Spacer(Modifier.height(LabSpacing.l)) }

        item(key = "header") {
            TableHeader(table, hScroll)
        }
        itemsIndexed(List(table.rows) { it }, key = { _, row -> "row$row" }) { _, row ->
            TableRow(
                table = table,
                row = row,
                scroll = hScroll,
                selected = state.selectedIndex,
                onSelect = component::onSelect
            )
        }
    }
}

@Composable
private fun TableHeader(table: PhiTable, scroll: ScrollState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LabSpacing.screen),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell("z", Z_COLUMN_WIDTH)
        Row(modifier = Modifier.horizontalScroll(scroll)) {
            repeat(table.columns) { col ->
                HeaderCell(NumberFormatter.fixed(col * table.step, 2), LabDimens.tableCellWidth)
            }
        }
    }
}

@Composable
private fun HeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(
        modifier = Modifier
            .width(width)
            .height(LabDimens.tableCellHeight),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = LabTheme.type.label, color = LabTheme.colors.primaryText)
    }
}

@Composable
private fun TableRow(
    table: PhiTable,
    row: Int,
    scroll: ScrollState,
    selected: Int?,
    onSelect: (Int) -> Unit,
) {
    val rowSelected = selected != null && selected / table.columns == row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LabSpacing.screen)
            .clip(LabShapes.cell)
            .background(if (row % 5 == 4) LabTheme.colors.card else LabTheme.colors.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(Z_COLUMN_WIDTH)
                .height(LabDimens.tableCellHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                NumberFormatter.fixed(table.zAt(row * table.columns), 1),
                style = LabTheme.type.label,
                color = if (rowSelected) LabTheme.colors.primary else LabTheme.colors.textSecondary
            )
        }
        Row(
            modifier = Modifier.horizontalScroll(scroll),
            horizontalArrangement = Arrangement.Start
        ) {
            repeat(table.columns) { col ->
                val index = table.cell(row, col)
                if (index < table.size) {
                    val isSelected = index == selected
                    val text = remember(index) { NumberFormatter.tableNotation(table.valueAt(index), table.decimalsAt(index)) }
                    Box(
                        modifier = Modifier
                            .width(LabDimens.tableCellWidth)
                            .height(LabDimens.tableCellHeight)
                            .padding(2.dp)
                            .clip(LabShapes.cell)
                            .background(if (isSelected) LabTheme.colors.primary else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable { onSelect(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            style = LabTheme.type.bodySmall,
                            color = if (isSelected) LabTheme.colors.onPrimary else LabTheme.colors.text
                        )
                    }
                }
            }
        }
    }
}

