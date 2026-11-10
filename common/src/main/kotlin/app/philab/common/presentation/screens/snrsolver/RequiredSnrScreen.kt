package app.philab.common.presentation.screens.snrsolver

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.molecules.StepsBlock
import app.philab.common.presentation.theme.LabTheme

@Composable
fun RequiredSnrScreen(component: RequiredSnrComponent) {
    val state by component.model.collectAsState()

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Требуемое ОСШ", "равновероятные символы, γ опт.") },
                leftActions = { BackButton(component::onBack) },
                rightActions = { SaveAction(enabled = state.result != null, onClick = component::onSave) }
            )
        }
    ) {
        item(key = "input") {
            SectionCard(title = "Заданная вероятность ошибки") {
                LabTextField(
                    value = state.peText,
                    onValueChange = component::onPeChange,
                    label = "Pe",
                    supporting = "например 10^-4 или 1e-5",
                    error = state.error != null,
                )
                LabTextField(
                    value = state.pskDeviationText,
                    onValueChange = component::onPskDeviationChange,
                    label = "Девиация фазы ФТ, ° (доп. строка, если ≠ 180)",
                )
                if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth(), color = LabTheme.colors.primary)
                state.error?.let { ErrorText(it) }
            }
        }
        state.result?.let { r ->
            item(key = "rows") {
                SectionCard(title = "Требуемое 2E/N₀") {
                    r.rows.forEach { row ->
                        ResultRow(
                            label = row.title,
                            value = "${NumberFormatter.format(row.q, 4)} · ${NumberFormatter.fixed(row.db, 2)} дБ",
                        )
                    }
                }
            }
            item(key = "diff") {
                SectionCard(title = "Разница") {
                    r.comparisons.forEach { c -> ResultRow(c.title, NumberFormatter.db(c.deltaDb), emphasized = false) }
                    StepsBlock(r.steps + r.rows.map { "${it.title}: ${it.formula}" })
                }
            }
        }
    }
}
