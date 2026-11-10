package app.philab.common.presentation.screens.tools

import com.arkivanov.decompose.ComponentContext
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory

/** Stateless list of tools — only navigation callbacks, so it needs no Store. */
class DefaultToolsComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onOpenRequiredSnr") private val onOpenRequiredSnr: () -> Unit,
    @Assisted("onOpenBlock") private val onOpenBlock: () -> Unit,
    @Assisted("onOpenDpsk") private val onOpenDpsk: () -> Unit,
    @Assisted("onOpenMatchedFilter") private val onOpenMatchedFilter: () -> Unit,
    @Assisted("onOpenConverter") private val onOpenConverter: () -> Unit,
    @Assisted("onOpenFormulas") private val onOpenFormulas: () -> Unit,
    @Assisted("onOpenTable") private val onOpenTable: () -> Unit,
) : ToolsComponent, ComponentContext by componentContext {

    override fun onOpenRequiredSnr() = onOpenRequiredSnr.invoke()
    override fun onOpenBlock() = onOpenBlock.invoke()
    override fun onOpenDpsk() = onOpenDpsk.invoke()
    override fun onOpenMatchedFilter() = onOpenMatchedFilter.invoke()
    override fun onOpenConverter() = onOpenConverter.invoke()
    override fun onOpenFormulas() = onOpenFormulas.invoke()
    override fun onOpenTable() = onOpenTable.invoke()

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onOpenRequiredSnr") onOpenRequiredSnr: () -> Unit,
            @Assisted("onOpenBlock") onOpenBlock: () -> Unit,
            @Assisted("onOpenDpsk") onOpenDpsk: () -> Unit,
            @Assisted("onOpenMatchedFilter") onOpenMatchedFilter: () -> Unit,
            @Assisted("onOpenConverter") onOpenConverter: () -> Unit,
            @Assisted("onOpenFormulas") onOpenFormulas: () -> Unit,
            @Assisted("onOpenTable") onOpenTable: () -> Unit,
        ): DefaultToolsComponent
    }
}
