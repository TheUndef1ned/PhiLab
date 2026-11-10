package app.philab.common.presentation.screens.snrsolver

import kotlinx.coroutines.flow.StateFlow

interface RequiredSnrComponent {
    val model: StateFlow<RequiredSnrStore.State>
    fun onBack()
    fun onPeChange(text: String)
    fun onPskDeviationChange(text: String)
    fun onSave()
}
