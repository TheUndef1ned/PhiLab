package app.philab.common.presentation.screens.block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.snr.SnrUnit
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
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun BlockScreen(component: BlockComponent) {
    val state by component.model.collectAsState()

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Команды и синхрослова", "поэлементный приём") },
                leftActions = { BackButton(component::onBack) },
                rightActions = { SaveAction(enabled = state.result != null, onClick = component::onSave) }
            )
        }
    ) {
        item(key = "word") {
            SectionCard(title = "Слово") {
                Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                    LabTextField(state.lengthText, component::onLengthChange, "Длина N", Modifier.weight(1f))
                    LabTextField(
                        state.maxErrorsText,
                        component::onMaxErrorsChange,
                        "Допустимо ошибок k",
                        Modifier.weight(1f),
                        supporting = "0 — все верно",
                    )
                }
                LabTextField(
                    value = state.targetText,
                    onValueChange = component::onTargetChange,
                    label = "Требуемая вероятность (для числа повторов)",
                    supporting = "оставьте пустым, если не нужно",
                )
            }
        }
        item(key = "symbol") {
            SectionCard(title = "Ошибка на символ p") {
                ChipRow(
                    items = listOf(false, true),
                    selected = state.manualP,
                    label = { if (it) "Задать p" else "Из метода и ОСШ" },
                    onSelect = component::onManualP,
                )
                if (state.manualP) {
                    LabTextField(state.pText, component::onPChange, "p")
                } else {
                    ChipRow(
                        items = Coherence.entries,
                        selected = state.coherence,
                        label = { it.title },
                        onSelect = component::onCoherence,
                    )
                    ChipRow(
                        items = listOf(Modulation.AT, Modulation.FSK, Modulation.PSK),
                        selected = state.modulation,
                        label = { it.short },
                        onSelect = component::onModulation,
                        enabled = { it != Modulation.PSK || state.coherence == Coherence.COHERENT },
                    )
                    ChipRow(
                        items = listOf(SnrUnit.TWO_E_N0, SnrUnit.E_N0, SnrUnit.DB_TWO_E_N0),
                        selected = state.snrUnit,
                        label = { it.label },
                        onSelect = component::onSnrUnit,
                    )
                    LabTextField(
                        value = state.snrText,
                        onValueChange = component::onSnrChange,
                        label = "ОСШ, ${state.snrUnit.label}",
                        supporting = "ФМ-2 = ФТ, ЧМ-2 = ЧТ; «E/N₀ = 2» — выберите E/N₀",
                    )
                }
                state.symbolP?.let { ResultRow("p (на символ)", NumberFormatter.probability(it)) }
                StepsBlock(state.symbolSteps, title = "Как получена p", initiallyExpanded = false)
            }
        }
        item(key = "result") {
            SectionCard(title = "Результат") {
                state.error?.let { ErrorText(it) }
                state.result?.let { r ->
                    AnswerBubble(
                        label = "Вероятность правильного приёма слова",
                        value = NumberFormatter.probability(r.pCorrect),
                        caption = "не более ${state.maxErrorsText} ошибок из ${state.lengthText}",
                    )
                    ResultRow("Вероятность отказа", NumberFormatter.probability(r.pFail))
                    r.repetitions?.let { ResultRow("Повторов нужно", it.toString(), emphasized = true) }
                    r.pAfterRepetitions?.let { ResultRow("P после повторов", NumberFormatter.probability(it)) }
                    ResultRow("Срабатывание по шумам (½ на элемент)", NumberFormatter.probability(r.falseTriggerHalf))
                    StepsBlock(r.steps)
                }
            }
        }
    }
}
