package app.philab.common.presentation.screens.matchedfilter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.philab.common.domain.matchedfilter.MatchedFilterUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.AnswerBubble
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.molecules.StepsBlock
import app.philab.common.presentation.components.organisms.chart.ChartPoint
import app.philab.common.presentation.components.organisms.chart.ChartSeries
import app.philab.common.presentation.components.organisms.chart.LineChart
import app.philab.common.presentation.theme.LabTheme

@Composable
fun MatchedFilterScreen(component: MatchedFilterComponent) {
    val state by component.model.collectAsState()
    val isDuration = state.mode == MatchedFilterUseCase.Mode.DURATION

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("СФ и коррелятор", "потери от рассогласования") },
                leftActions = { BackButton(component::onBack) },
                rightActions = { SaveAction(enabled = state.result != null, onClick = component::onSave) }
            )
        }
    ) {
        item(key = "input") {
            SectionCard(title = "Параметры") {
                ChipRow(
                    items = MatchedFilterUseCase.Mode.entries,
                    selected = state.mode,
                    label = { if (it == MatchedFilterUseCase.Mode.DURATION) "Длительность T ≠ T₀" else "Фаза опоры φ" },
                    onSelect = component::onMode,
                )
                LabTextField(
                    value = state.qText,
                    onValueChange = component::onQChange,
                    label = if (isDuration) "2E₀/N₀ (максимум на выходе СФ)" else "2E/N₀ без расстройки",
                )
                if (isDuration) {
                    LabTextField(
                        value = state.ratioText,
                        onValueChange = component::onRatioChange,
                        label = "T/T₀ (энергия импульса та же)",
                    )
                } else {
                    LabTextField(
                        value = state.phaseText,
                        onValueChange = component::onPhaseChange,
                        label = "Расстройка φ, градусы",
                    )
                }
            }
        }
        item(key = "result") {
            SectionCard(title = "Результат") {
                state.error?.let { ErrorText(it) }
                state.result?.let { r ->
                    AnswerBubble(
                        label = "ОСШ на выходе в момент отсчёта",
                        value = NumberFormatter.format(r.qOut, 5),
                        caption = "q/q_max = ${NumberFormatter.format(r.gain, 5)} · потери ${NumberFormatter.db(-r.lossDb)}",
                    )
                    r.pskPe?.let { ResultRow("Pe ФТ с такой опорой", NumberFormatter.probability(it)) }
                    LineChart(
                        series = listOf(
                            ChartSeries(
                                name = if (isDuration) "q/q_max от T/T₀" else "q/q_max = cos²φ",
                                points = r.curve.map { ChartPoint(it.x, it.gain) },
                                color = LabTheme.colors.primary,
                            )
                        ),
                        xLabel = if (isDuration) "T/T₀" else "φ, °",
                        yLabel = "q/q_max",
                        logY = false,
                    )
                    StepsBlock(r.steps)
                }
            }
        }
    }
}
