package app.philab.common.presentation.screens.formulas

import app.philab.common.domain.formulas.FormulaCatalog

interface FormulasComponent {
    val sections: List<FormulaCatalog.Section>
    fun onBack()
}
