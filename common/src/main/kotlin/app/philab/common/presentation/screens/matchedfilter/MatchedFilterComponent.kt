package app.philab.common.presentation.screens.matchedfilter

import app.philab.common.domain.matchedfilter.MatchedFilterUseCase
import kotlinx.coroutines.flow.StateFlow

interface MatchedFilterComponent {
    val model: StateFlow<MatchedFilterStore.State>
    fun onBack()
    fun onMode(mode: MatchedFilterUseCase.Mode)
    fun onQChange(text: String)
    fun onRatioChange(text: String)
    fun onPhaseChange(text: String)
    fun onSave()
}
