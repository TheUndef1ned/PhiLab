package app.philab.root

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import app.philab.common.presentation.screens.block.BlockComponent
import app.philab.common.presentation.screens.converter.ConverterComponent
import app.philab.common.presentation.screens.dpsk.DpskComponent
import app.philab.common.presentation.screens.formulas.FormulasComponent
import app.philab.common.presentation.screens.main.MainComponent
import app.philab.common.presentation.screens.matchedfilter.MatchedFilterComponent
import app.philab.common.presentation.screens.phitable.PhiTableComponent
import app.philab.common.presentation.screens.plot.PlotComponent
import app.philab.common.presentation.screens.snrsolver.RequiredSnrComponent
import kotlinx.coroutines.flow.StateFlow

interface RootComponent {

    val stack: Value<ChildStack<*, Child>>
    val toast: StateFlow<String?>

    sealed interface Child {
        data class Main(val component: MainComponent) : Child
        data class PhiTable(val component: PhiTableComponent) : Child
        data class Plot(val component: PlotComponent) : Child
        data class RequiredSnr(val component: RequiredSnrComponent) : Child
        data class Block(val component: BlockComponent) : Child
        data class Dpsk(val component: DpskComponent) : Child
        data class MatchedFilter(val component: MatchedFilterComponent) : Child
        data class Converter(val component: ConverterComponent) : Child
        data class Formulas(val component: FormulasComponent) : Child
    }
}
