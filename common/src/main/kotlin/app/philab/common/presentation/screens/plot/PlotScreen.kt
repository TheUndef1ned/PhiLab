package app.philab.common.presentation.screens.plot

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.SweepOutput
import app.philab.common.domain.reception.model.ThresholdMode
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.organisms.chart.ChartPoint
import app.philab.common.presentation.components.organisms.chart.ChartSeries
import app.philab.common.presentation.components.organisms.chart.LineChart
import app.philab.common.presentation.components.organisms.chart.chartColors
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun PlotScreen(component: PlotComponent) {
    val state by component.model.collectAsState()
    val colors = chartColors()

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Построить зависимость", describe(state.base)) },
                leftActions = { BackButton(component::onBack) }
            )
        }
    ) {
        item(key = "controls") {
            SectionCard(title = "Ось X") {
                ChipRow(
                    items = state.availableVariables,
                    selected = state.variable,
                    label = { it.title },
                    onSelect = component::onVariable,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                    LabTextField(state.fromText, component::onFromChange, "от", Modifier.weight(1f))
                    LabTextField(state.toText, component::onToChange, "до", Modifier.weight(1f))
                }
                ChipRow(
                    items = SweepOutput.entries,
                    selected = state.output,
                    label = { it.title },
                    onSelect = component::onOutput,
                )
                ChipRow(
                    items = listOf(true, false),
                    selected = state.logY,
                    label = { if (it) "Лог. шкала Y" else "Линейная Y" },
                    onSelect = component::onLogY,
                )
                if (state.base.threshold != ThresholdMode.Optimal) {
                    ChipRow(
                        items = listOf(true, false),
                        selected = state.compare,
                        label = { if (it) "Сравнить с γ опт." else "Без сравнения" },
                        onSelect = component::onCompare,
                    )
                }
            }
        }

        item(key = "chart") {
            SectionCard(title = "${state.output.title}(${state.variable.axis})") {
                if (state.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = LabTheme.colors.primary)
                state.error?.let { ErrorText(it) }
                state.result?.let { r ->
                    LineChart(
                        series = r.series.mapIndexed { i, s ->
                            ChartSeries(
                                name = s.name,
                                points = s.points.map { ChartPoint(it.x, it.y) },
                                color = colors[i % colors.size],
                                dashed = i > 0,
                            )
                        },
                        xLabel = state.variable.axis,
                        yLabel = state.output.title,
                        logY = state.logY,
                    )
                    Text(
                        text = "Нажмите или проведите по графику, чтобы увидеть значения",
                        style = LabTheme.type.hint,
                        color = LabTheme.colors.textMuted
                    )
                }
            }
        }

        state.result?.let { r ->
            val main = r.series.firstOrNull()?.points.orEmpty()
            if (main.isNotEmpty()) {
                item(key = "table") {
                    SectionCard(title = "Таблица значений") {
                        val step = (main.size - 1) / 10
                        val rows = if (step <= 0) main else main.filterIndexed { i, _ -> i % step == 0 }
                        rows.forEach { p ->
                            val extra = r.series.drop(1).joinToString { s ->
                                s.points.minByOrNull { kotlin.math.abs(it.x - p.x) }?.let { NumberFormatter.probability(it.y) } ?: "—"
                            }
                            ResultRow(
                                label = "${state.variable.axis} = ${NumberFormatter.format(p.x, 4)}",
                                value = NumberFormatter.probability(p.y) + if (extra.isNotEmpty()) "  |  $extra" else "",
                            )
                        }
                        minPoint(main)?.let { m ->
                            Text(
                                text = "Минимум ${state.output.title} = ${NumberFormatter.probability(m.y)} при " +
                                    "${state.variable.axis} = ${NumberFormatter.format(m.x, 4)}",
                                style = LabTheme.type.bodySmall,
                                color = LabTheme.colors.primaryText
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun minPoint(points: List<app.philab.common.domain.reception.model.SweepPoint>) = points.minByOrNull { it.y }

private fun describe(p: ReceptionParams): String {
    val threshold = when (val t = p.threshold) {
        ThresholdMode.Optimal -> "γ опт."
        is ThresholdMode.Fixed -> "γ = ${NumberFormatter.format(t.gamma, 4)}"
        is ThresholdMode.EnergyFactor -> "γ = ${NumberFormatter.format(t.factor, 4)}·E/N₀"
        is ThresholdMode.NeymanPearson -> "α = ${NumberFormatter.probability(t.alpha)}"
    }
    return "${p.modulation.short} · ${p.coherence.title.lowercase()} · 2E/N₀ = ${NumberFormatter.format(p.q, 4)} · " +
        "p₀ = ${NumberFormatter.format(p.p0, 3)} · $threshold"
}
