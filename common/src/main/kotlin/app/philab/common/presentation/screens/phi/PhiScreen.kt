package app.philab.common.presentation.screens.phi

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.phi.model.PhiMethod
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabButton
import app.philab.common.presentation.components.atoms.LabButtonStyle
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.AnswerBubble
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.molecules.StepsBlock
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun PhiScreen(component: PhiComponent) {
    val state by component.model.collectAsState()

    ScreenScaffold(
        insideTabs = true,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Интеграл вероятности", "Φ(z) = 1/√(2π) ∫ e^(−x²/2) dx") },
                rightActions = {
                    IconButton(onClick = component::onOpenTable) {
                        Icon(Icons.Outlined.TableChart, contentDescription = "Таблица", tint = LabTheme.colors.primary)
                    }
                }
            )
        }
    ) {
        item(key = "lookup") {
            SectionCard(
                title = "Φ(z) по таблице",
                actions = { SaveAction(enabled = state.lookup != null, onClick = component::onSaveLookup) }
            ) {
                LabTextField(
                    value = state.zText,
                    onValueChange = component::onZChange,
                    label = "Аргумент z",
                    placeholder = "например 2,24 или −1/3",
                    keyboardType = KeyboardType.Text,
                    error = state.zError != null,
                    supporting = state.zError ?: "Можно вводить выражения: 2/√5, (5−3)/√5",
                )
                state.lookup?.let { r ->
                    val book = if (r.cellIndex != null && r.cellDecimals != null && !r.usedSymmetry &&
                        r.method == PhiMethod.TABLE
                    ) {
                        NumberFormatter.tableNotation(r.phi, r.cellDecimals)
                    } else {
                        NumberFormatter.format(r.phi, 8)
                    }
                    AnswerBubble(
                        label = "Φ(${NumberFormatter.format(r.z)})",
                        value = book,
                        caption = r.method.label + if (r.usedSymmetry) " + симметрия" else "",
                    )
                    ResultRow("1 − Φ(z)", NumberFormatter.probability(NormalDistribution.upperTail(r.z)))
                    ResultRow("Точное значение", NumberFormatter.format(r.exact, 9))
                    state.osipov?.let { ResultRow("Аппрокс. Осипова", NumberFormatter.format(it, 6)) }
                    StepsBlock(r.steps, initiallyExpanded = false)
                    LabButton(
                        text = "Показать в таблице",
                        icon = Icons.Outlined.TableChart,
                        style = LabButtonStyle.Secondary,
                        onClick = component::onOpenTable,
                    )
                }
            }
        }

        item(key = "inverse") {
            SectionCard(
                title = "Обратная задача: z по Φ",
                actions = { SaveAction(enabled = state.inverse != null, onClick = component::onSaveInverse) }
            ) {
                LabTextField(
                    value = state.inverseText,
                    onValueChange = component::onInverseChange,
                    label = "Значение Φ(z)",
                    placeholder = "например 1 − 10^-3",
                    error = state.inverseError != null,
                    supporting = state.inverseError ?: "Для Неймана–Пирсона: Φ = 1 − α",
                )
                state.inverse?.let { r ->
                    AnswerBubble(label = "z", value = NumberFormatter.format(r.z, 5))
                    ResultRow("Точно", NumberFormatter.format(r.exactZ, 7))
                    StepsBlock(r.steps, initiallyExpanded = false)
                }
            }
        }

        item(key = "normal") {
            SectionCard(
                title = "Нормальная величина N(m, D)",
                actions = { SaveAction(enabled = state.normal != null, onClick = component::onSaveNormal) }
            ) {
                Text(
                    text = "Вероятность превышения порога (практика 9)",
                    style = LabTheme.type.bodySmall,
                    color = LabTheme.colors.textMuted
                )
                Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                    LabTextField(
                        value = state.meanText,
                        onValueChange = component::onMeanChange,
                        label = "m (среднее)",
                        modifier = Modifier.weight(1f)
                    )
                    LabTextField(
                        value = state.varianceText,
                        onValueChange = component::onVarianceChange,
                        label = "D (дисперсия)",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                    LabTextField(
                        value = state.aText,
                        onValueChange = component::onAChange,
                        label = "Порог a",
                        modifier = Modifier.weight(1f)
                    )
                    LabTextField(
                        value = state.bText,
                        onValueChange = component::onBChange,
                        label = "b (необяз.)",
                        placeholder = "для a < X < b",
                        modifier = Modifier.weight(1f)
                    )
                }
                state.normalError?.let { ErrorText(it) }
                state.normal?.let { r ->
                    ResultRow("P(X > a)", NumberFormatter.probability(r.greater), emphasized = true)
                    ResultRow("P(X < a)", NumberFormatter.probability(r.less))
                    r.between?.let { ResultRow("P(a < X < b)", NumberFormatter.probability(it)) }
                    StepsBlock(r.steps)
                }
            }
        }
    }
}
