package app.philab.common.presentation.screens.block

import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.snr.SnrUnit
import kotlinx.coroutines.flow.StateFlow

interface BlockComponent {
    val model: StateFlow<BlockStore.State>
    fun onBack()
    fun onLengthChange(text: String)
    fun onMaxErrorsChange(text: String)
    fun onManualP(manual: Boolean)
    fun onPChange(text: String)
    fun onModulation(value: Modulation)
    fun onCoherence(value: Coherence)
    fun onSnrChange(text: String)
    fun onSnrUnit(unit: SnrUnit)
    fun onTargetChange(text: String)
    fun onSave()
}
