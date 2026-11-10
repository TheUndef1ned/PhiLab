package app.philab.common.presentation.screens.converter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard

@Composable
fun ConverterScreen(component: ConverterComponent) {
    val state by component.model.collectAsState()
    val snr = state.snr

    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Конвертер ОСШ") },
                leftActions = { BackButton(component::onBack) },
                rightActions = { SaveAction(enabled = snr != null, onClick = component::onSave) }
            )
        }
    ) {
        item(key = "input") {
            SectionCard(title = "Исходное значение") {
                ChipRow(SnrUnit.entries, state.unit, { it.label }, component::onUnit)
                LabTextField(state.valueText, component::onValueChange, "Значение, ${state.unit.label}")
            }
        }
        item(key = "out") {
            SectionCard(title = "Во всех единицах") {
                if (snr == null) {
                    ErrorText("Введите корректное значение")
                } else {
                    ResultRow("2E/N₀", NumberFormatter.format(snr.q, 6), emphasized = true)
                    ResultRow("E/N₀", NumberFormatter.format(snr.eOverN0, 6))
                    ResultRow("√(2E/N₀)", NumberFormatter.format(snr.amplitude, 6))
                    ResultRow("2E/N₀, дБ", NumberFormatter.db(snr.db))
                    ResultRow("E/N₀, дБ", NumberFormatter.db(snr.db - 10 * kotlin.math.log10(2.0)))
                }
            }
        }
    }
}
