package app.philab.common.presentation.screens.phitable

import kotlinx.coroutines.flow.StateFlow

interface PhiTableComponent {
    val model: StateFlow<PhiTableStore.State>
    fun onBack()
    fun onSelect(index: Int)
}
