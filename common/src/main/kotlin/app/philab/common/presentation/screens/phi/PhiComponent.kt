package app.philab.common.presentation.screens.phi

import kotlinx.coroutines.flow.StateFlow

interface PhiComponent {
    val model: StateFlow<PhiStore.State>
    fun onZChange(text: String)
    fun onInverseChange(text: String)
    fun onMeanChange(text: String)
    fun onVarianceChange(text: String)
    fun onAChange(text: String)
    fun onBChange(text: String)
    fun onSaveLookup()
    fun onSaveInverse()
    fun onSaveNormal()
    fun onOpenTable()
}
