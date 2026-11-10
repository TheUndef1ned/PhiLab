package app.philab.common.presentation.screens.dpsk

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.AnswerBubble
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.molecules.StepsBlock
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun DpskScreen(component: DpskComponent) {
    val state by component.model.collectAsState()

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("ОФТ", "относительная фазовая телеграфия") },
                leftActions = { BackButton(component::onBack) },
                rightActions = { SaveAction(enabled = state.output != null, onClick = component::onSave) }
            )
        }
    ) {
        item(key = "input") {
            SectionCard(title = "Ввод") {
                ChipRow(DpskMode.entries, state.mode, { it.title }, component::onMode)
                Text(
                    "λ = 1 — фаза как на предыдущем такте, λ = 0 (−1) — фаза меняется на π",
                    style = LabTheme.type.bodySmall,
                    color = LabTheme.colors.textMuted
                )
                if (state.mode == DpskMode.ENCODE) {
                    LabTextField(
                        value = state.symbolsText,
                        onValueChange = component::onSymbolsChange,
                        label = "Информационные символы",
                        supporting = "через пробел: 1 −1 −1 … или 1 0 0 …",
                        singleLine = false,
                    )
                    ChipRow(
                        items = listOf(0, 1),
                        selected = state.referencePhase,
                        label = { "Опорная фаза ${if (it == 0) "0" else "π"}" },
                        onSelect = component::onReference,
                    )
                } else {
                    LabTextField(
                        value = state.phasesText,
                        onValueChange = component::onPhasesChange,
                        label = "Принятые начальные фазы",
                        supporting = "через пробел: 0 0 π π … (можно 180)",
                        singleLine = false,
                    )
                }
            }
        }
        item(key = "output") {
            SectionCard(title = if (state.mode == DpskMode.ENCODE) "Фазы на тактах" else "Переданные символы") {
                state.error?.let { ErrorText(it) }
                state.output?.let { out ->
                    AnswerBubble(label = "Ответ", value = out)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(LabSpacing.xs)
                    ) {
                        state.outputSequence.forEachIndexed { i, v ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            if (v == "π" || v == "0" && state.mode == DpskMode.DECODE) LabTheme.colors.primarySoft
                                            else LabTheme.colors.raised,
                                            LabShapes.cell
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(v, style = LabTheme.type.title, color = LabTheme.colors.text)
                                }
                                Text(
                                    text = if (state.mode == DpskMode.ENCODE) "$i" else "${i + 1}",
                                    style = LabTheme.type.hint,
                                    color = LabTheme.colors.textMuted,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                    }
                    StepsBlock(state.steps)
                }
            }
        }
    }
}
