package app.philab.common.presentation.screens.formulas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.BackButton
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.theme.LabShapes
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun FormulasScreen(component: FormulasComponent) {
    ScreenScaffold(
        insideTabs = false,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Шпаргалка формул", "ОАПСРС") },
                leftActions = { BackButton(component::onBack) }
            )
        }
    ) {
        items(component.sections, key = { it.title }) { section ->
            SectionCard(title = section.title) {
                if (section.source.isNotEmpty()) {
                    Text(section.source, style = LabTheme.type.caption, color = LabTheme.colors.textMuted)
                }
                section.formulas.forEach { f ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(f.title, style = LabTheme.type.label, color = LabTheme.colors.textSecondary)
                        SelectionContainer {
                            Text(
                                text = f.body,
                                style = LabTheme.type.body,
                                color = LabTheme.colors.onHighlight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = LabSpacing.xs)
                                    .background(LabTheme.colors.highlight.copy(alpha = 0.5f), LabShapes.formula)
                                    .padding(horizontal = LabSpacing.m, vertical = LabSpacing.s)
                            )
                        }
                        f.note?.let {
                            Text(it, style = LabTheme.type.hint, color = LabTheme.colors.textMuted, modifier = Modifier.padding(top = LabSpacing.xs))
                        }
                    }
                }
            }
        }
    }
}

