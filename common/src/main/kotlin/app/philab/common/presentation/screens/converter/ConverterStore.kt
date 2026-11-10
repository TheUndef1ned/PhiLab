package app.philab.common.presentation.screens.converter

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import app.philab.common.domain.history.model.CalcKind
import app.philab.common.domain.history.usecase.SaveCalcRecordUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.domain.snr.Snr
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.screens.converter.ConverterStore.Intent
import app.philab.common.presentation.screens.converter.ConverterStore.Label
import app.philab.common.presentation.screens.converter.ConverterStore.State
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ConverterStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data class ChangeValue(val text: String) : Intent
        data class SetUnit(val unit: SnrUnit) : Intent
        data object Save : Intent
    }

    data class State(
        val valueText: String = "10",
        val unit: SnrUnit = SnrUnit.TWO_E_N0,
    ) {
        val snr: Snr? get() = ExpressionParser.parse(valueText)?.let { v ->
            if (unit != SnrUnit.DB_TWO_E_N0 && v < 0) null else Snr.of(v, unit)
        }
    }

    sealed interface Label {
        data class ShowToast(val text: String) : Label
    }
}

class ConverterStoreFactory @Inject constructor(
    private val storeFactory: StoreFactory,
    private val saveRecord: SaveCalcRecordUseCase,
) {
    fun create(): ConverterStore =
        object : ConverterStore, Store<Intent, State, Label> by storeFactory.create(
            name = "ConverterStore",
            initialState = State(),
            executorFactory = ::ExecutorImpl,
            reducer = ReducerImpl,
        ) {}

    private sealed interface Msg {
        data class Value(val text: String) : Msg
        data class UnitChanged(val unit: SnrUnit) : Msg
    }

    private inner class ExecutorImpl : CoroutineExecutor<Intent, Nothing, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.ChangeValue -> dispatch(Msg.Value(intent.text))
                is Intent.SetUnit -> dispatch(Msg.UnitChanged(intent.unit))
                Intent.Save -> {
                    val snr = state().snr ?: return
                    val text = SnrUnit.entries.joinToString("; ") { "${it.label} = ${NumberFormatter.format(snr.valueIn(it), 5)}" }
                    scope.launch {
                        saveRecord(CalcKind.CONVERTER, "ОСШ: ${state().valueText} (${state().unit.label})", state().valueText, text)
                            .onSuccess { publish(Label.ShowToast("Сохранено в историю")) }
                    }
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State = when (msg) {
            is Msg.Value -> copy(valueText = msg.text)
            is Msg.UnitChanged -> copy(unit = msg.unit)
        }
    }
}
