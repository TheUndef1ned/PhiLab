package app.philab.common.presentation.screens.converter

import app.philab.common.domain.snr.SnrUnit
import kotlinx.coroutines.flow.StateFlow

interface ConverterComponent {
    val model: StateFlow<ConverterStore.State>
    fun onBack()
    fun onValueChange(text: String)
    fun onUnit(unit: SnrUnit)
    fun onSave()
}
