package app.philab.common.presentation.screens.history

import app.philab.common.domain.history.model.CalcKind
import kotlinx.coroutines.flow.StateFlow

interface HistoryComponent {
    val model: StateFlow<HistoryStore.State>
    fun onDelete(id: String)
    fun onClear()
    fun onFilter(kind: CalcKind?)
}
