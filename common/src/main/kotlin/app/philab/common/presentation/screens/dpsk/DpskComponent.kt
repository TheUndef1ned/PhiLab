package app.philab.common.presentation.screens.dpsk

import kotlinx.coroutines.flow.StateFlow

interface DpskComponent {
    val model: StateFlow<DpskStore.State>
    fun onBack()
    fun onMode(mode: DpskMode)
    fun onSymbolsChange(text: String)
    fun onPhasesChange(text: String)
    fun onReference(phase: Int)
    fun onSave()
}
