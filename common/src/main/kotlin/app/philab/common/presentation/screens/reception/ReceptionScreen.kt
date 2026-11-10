package app.philab.common.presentation.screens.reception

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ShowChart
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import app.philab.common.domain.snr.Snr
import app.philab.common.domain.snr.SnrUnit
import app.philab.common.presentation.components.TopBarTitle
import app.philab.common.presentation.components.LabTopBar
import app.philab.common.presentation.components.atoms.ChipRow
import app.philab.common.presentation.components.atoms.SaveAction
import app.philab.common.presentation.components.atoms.LabButton
import app.philab.common.presentation.components.atoms.LabTextField
import app.philab.common.presentation.components.molecules.AnswerBubble
import app.philab.common.presentation.components.molecules.ErrorText
import app.philab.common.presentation.components.molecules.NotesBlock
import app.philab.common.presentation.components.molecules.ResultRow
import app.philab.common.presentation.components.molecules.ScreenScaffold
import app.philab.common.presentation.components.molecules.SectionCard
import app.philab.common.presentation.components.molecules.StepsBlock
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing

@Composable
fun ReceptionScreen(component: ReceptionComponent) {
    val state by component.model.collectAsState()

    ScreenScaffold(
        insideTabs = true,
        topBar = {
            LabTopBar(
                title = { TopBarTitle("Приём двоичных сигналов", "Pe, α, β, порог γ") },
            )
        }
    ) {
        item(key = "signals") {
            SectionCard(title = "Сигналы") {
                ChipRow(
                    items = Coherence.entries,
                    selected = state.coherence,
                    label = { it.title },
                    onSelect = component::onCoherence,
                )
                ChipRow(
                    items = state.availableModulations,
                    selected = state.modulation,
                    label = { it.short },
                    onSelect = component::onModulation,
                )
                Text(
                    text = state.modulation.title,
                    style = LabTheme.type.bodySmall,
                    color = LabTheme.colors.textMuted
                )
                ModulationFields(state, component)
            }
        }

        item(key = "channel") {
            SectionCard(title = "Канал") {
                ChipRow(
                    items = SnrUnit.entries,
                    selected = state.snrUnit,
                    label = { it.label },
                    onSelect = component::onSnrUnit,
                )
                val snrHint = ExpressionParser.parse(state.snrText)?.let { v ->
                    val snr = Snr.of(v, state.snrUnit)
                    "2E/N₀ = ${NumberFormatter.format(snr.q, 5)} · E/N₀ = ${NumberFormatter.format(snr.eOverN0, 5)} · " +
                        "${NumberFormatter.fixed(snr.db, 2)} дБ"
                }
                LabTextField(
                    value = state.snrText,
                    onValueChange = component::onSnrChange,
                    label = "Отношение сигнал/шум, ${state.snrUnit.label}",
                    supporting = snrHint,
                )
                LabTextField(
                    value = state.p0Text,
                    onValueChange = component::onP0Change,
                    label = "Априорная вероятность p₀ (λ = 0)",
                    supporting = ExpressionParser.parse(state.p0Text)?.let { "p₁ = ${NumberFormatter.format(1 - it, 6)}" }
                        ?: "например 3/7 для кода (4,3)",
                )
            }
        }

        item(key = "threshold") {
            SectionCard(title = "Порог") {
                ChipRow(
                    items = state.availableThresholds,
                    selected = state.thresholdKind,
                    label = { it.title },
                    onSelect = component::onThresholdKind,
                )
                when (state.thresholdKind) {
                    ThresholdKind.OPTIMAL -> Text(
                        text = if (state.coherence == Coherence.COHERENT) {
                            "Идеальный наблюдатель: γ = (E₁ − E₀)/N₀ + ln(p₀/p₁)"
                        } else {
                            "Пересечение плотностей Релея и Райса: γ* = Arg ln I₀(E/N₀ + ln p₀/p₁)/√(2E/N₀)"
                        },
                        style = LabTheme.type.bodySmall,
                        color = LabTheme.colors.textMuted
                    )
                    ThresholdKind.FIXED -> LabTextField(
                        value = state.thresholdText,
                        onValueChange = component::onThresholdChange,
                        label = if (state.coherence == Coherence.COHERENT) "γ (в единицах статистики l)" else "γ* (огибающая / СКО шума)",
                    )
                    ThresholdKind.FACTOR -> LabTextField(
                        value = state.thresholdText,
                        onValueChange = component::onThresholdChange,
                        label = "c в γ = c·E/N₀",
                        supporting = "«γ = E/(4N₀)» ⇒ c = 1/4",
                    )
                    ThresholdKind.NEYMAN_PEARSON -> LabTextField(
                        value = state.thresholdText,
                        onValueChange = component::onThresholdChange,
                        label = "Вероятность ложной тревоги α",
                        supporting = "например 10^-3",
                    )
                }
            }
        }

        item(key = "result") {
            SectionCard(
                title = "Результат",
                actions = { SaveAction(enabled = state.result != null, onClick = component::onSave) }
            ) {
                state.error?.let { ErrorText(it) }
                state.result?.let { r ->
                    AnswerBubble(
                        label = "Полная вероятность ошибки Pe",
                        value = NumberFormatter.probability(r.pe),
                        caption = r.approximatePe?.let { "≈ ½e^(−q/8) = ${NumberFormatter.probability(it)}" },
                    )
                    ResultRow("α (ложная тревога)", NumberFormatter.probability(r.alpha))
                    ResultRow("β (пропуск)", NumberFormatter.probability(r.beta))
                    ResultRow("Порог γ", NumberFormatter.format(r.gamma, 5))
                    ResultRow("γ оптимальный", NumberFormatter.format(r.gammaOptimal, 5))
                    if (r.peOptimal != r.pe) ResultRow("Pe при γ опт.", NumberFormatter.probability(r.peOptimal))
                    r.m1?.let { ResultRow("m₁", NumberFormatter.format(it, 5)) }
                    r.m0?.let { ResultRow("m₀", NumberFormatter.format(it, 5)) }
                    r.sigma?.let { ResultRow("σ", NumberFormatter.format(it, 5)) }
                    NotesBlock(r.notes)
                    StepsBlock(r.steps)
                    LabButton(
                        text = "Построить зависимость",
                        icon = Icons.AutoMirrored.Outlined.ShowChart,
                        onClick = component::onOpenPlot,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModulationFields(state: ReceptionStore.State, component: ReceptionComponent) {
    when (state.modulation) {
        Modulation.PSK -> if (state.coherence == Coherence.COHERENT) {
            LabTextField(
                value = state.pskDeviationText,
                onValueChange = component::onPskDeviationChange,
                label = "Девиация фазы Δφ, градусы",
                supporting = "ρ = cos Δφ; 180° — противоположные сигналы",
            )
        }

        Modulation.FSK -> if (state.coherence == Coherence.COHERENT) {
            ChipRow(
                items = listOf(true, false),
                selected = state.fskWide,
                label = { if (it) "|ω₁−ω₀|T ≫ 1 (ρ = 0)" else "задать ΔT" },
                onSelect = component::onFskWide,
            )
            if (!state.fskWide) {
                LabTextField(
                    value = state.fskDeltaTText,
                    onValueChange = component::onFskDeltaTChange,
                    label = "ΔT = (ω₁ − ω₀)·T, рад",
                    supporting = "ρ = sin(ΔT)/ΔT",
                )
            }
        }

        Modulation.CUSTOM -> {
            Text(
                text = "Энергии в долях E: E₁ = k₁E, E₀ = k₀E. Пример: s и −2s ⇒ k₁ = 1, k₀ = 4, ρ = −1",
                style = LabTheme.type.bodySmall,
                color = LabTheme.colors.textMuted
            )
            Row(horizontalArrangement = Arrangement.spacedBy(LabSpacing.s)) {
                LabTextField(state.k1Text, component::onK1Change, "E₁/E", Modifier.weight(1f))
                LabTextField(state.k0Text, component::onK0Change, "E₀/E", Modifier.weight(1f))
                LabTextField(state.rhoText, component::onRhoChange, "ρ", Modifier.weight(1f))
            }
        }

        Modulation.AT -> Unit
    }
}
