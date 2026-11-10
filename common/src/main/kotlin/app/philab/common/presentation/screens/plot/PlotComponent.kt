package app.philab.common.presentation.screens.plot

import app.philab.common.domain.reception.model.SweepOutput
import app.philab.common.domain.reception.model.SweepVariable
import kotlinx.coroutines.flow.StateFlow

interface PlotComponent {
    val model: StateFlow<PlotStore.State>
    fun onBack()
    fun onVariable(variable: SweepVariable)
    fun onOutput(output: SweepOutput)
    fun onFromChange(text: String)
    fun onToChange(text: String)
    fun onLogY(log: Boolean)
    fun onCompare(compare: Boolean)
}
