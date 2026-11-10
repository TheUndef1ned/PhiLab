package app.philab.common.presentation.screens.reception

import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.snr.SnrUnit
import kotlinx.coroutines.flow.StateFlow

interface ReceptionComponent {
    val model: StateFlow<ReceptionStore.State>
    fun onCoherence(value: Coherence)
    fun onModulation(value: Modulation)
    fun onSnrChange(text: String)
    fun onSnrUnit(unit: SnrUnit)
    fun onP0Change(text: String)
    fun onThresholdKind(kind: ThresholdKind)
    fun onThresholdChange(text: String)
    fun onPskDeviationChange(text: String)
    fun onFskWide(wide: Boolean)
    fun onFskDeltaTChange(text: String)
    fun onK1Change(text: String)
    fun onK0Change(text: String)
    fun onRhoChange(text: String)
    fun onSave()
    fun onOpenPlot()
}
