package app.philab.common.presentation.screens.tools

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.SettingsInputAntenna
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.ViewWeek
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.ToolGroup
import app.philab.common.presentation.components.molecules.ToolItem
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun ToolsScreen(component: ToolsComponent) {
    ScreenScaffold(
        insideTabs = true,
        topBar = { LabTopBar(title = { TopBarTitle("Инструменты") }) }
    ) {
        item { GroupTitle("Расчёты") }
        item {
            ToolGroup(
                listOf(
                    ToolItem(Icons.Outlined.SettingsInputAntenna, "Требуемое ОСШ", "Pe → 2E/N₀ для АТ/ЧТ/ФТ, разница в дБ", component::onOpenRequiredSnr),
                    ToolItem(Icons.Outlined.ViewWeek, "Команды и синхрослова", "N элементов, не более k ошибок, повторы", component::onOpenBlock),
                    ToolItem(Icons.Outlined.SwapVert, "ОФТ: кодер / декодер", "Символы ⇄ начальные фазы", component::onOpenDpsk),
                    ToolItem(Icons.Outlined.FilterAlt, "СФ и коррелятор", "Рассогласование по T и по фазе", component::onOpenMatchedFilter),
                    ToolItem(Icons.AutoMirrored.Outlined.CompareArrows, "Конвертер ОСШ", "дБ ⇄ разы, E/N₀ ⇄ 2E/N₀ ⇄ √(2E/N₀)", component::onOpenConverter),
                )
            )
        }
        item { GroupTitle("Справка") }
        item {
            ToolGroup(
                listOf(
                    ToolItem(Icons.Outlined.TableChart, "Таблица Φ(z)", "Приложение IV, z = 0…4,99", component::onOpenTable),
                    ToolItem(Icons.AutoMirrored.Outlined.MenuBook, "Шпаргалка формул", "Лекции 9–17, практика 1–9", component::onOpenFormulas),
                    ToolItem(Icons.Outlined.Functions, "Ввод выражений", "Во всех полях: 1/4, 10^-3, √5, ln(3/7), π/3", {}),
                )
            )
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text = text,
        style = LabTheme.type.label,
        color = LabTheme.colors.textMuted,
        modifier = Modifier.padding(horizontal = LabSpacing.xxl)
    )
}
