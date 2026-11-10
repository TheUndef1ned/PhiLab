package app.philab.common.presentation.screens.formulas

import com.arkivanov.decompose.ComponentContext
import app.philab.common.domain.formulas.FormulaCatalog
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.assisted.AssistedFactory

class DefaultFormulasComponent @AssistedInject constructor(
    @Assisted("componentContext") componentContext: ComponentContext,
    @Assisted("onBack") private val onBack: () -> Unit,
) : FormulasComponent, ComponentContext by componentContext {

    override val sections: List<FormulaCatalog.Section> = FormulaCatalog.sections

    override fun onBack() = onBack.invoke()

    @AssistedFactory
    interface Factory {
        fun create(
            @Assisted("componentContext") componentContext: ComponentContext,
            @Assisted("onBack") onBack: () -> Unit,
        ): DefaultFormulasComponent
    }
}
