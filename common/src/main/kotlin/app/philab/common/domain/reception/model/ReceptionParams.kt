package app.philab.common.domain.reception.model

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Serializable
enum class Modulation(val short: String, val title: String) {
    AT("АТ", "Амплитудная телеграфия"),
    FSK("ЧТ", "Частотная телеграфия"),
    PSK("ФТ", "Фазовая телеграфия"),
    CUSTOM("s₁/s₀", "Произвольная пара сигналов"),
}

@Serializable
enum class Coherence(val title: String) {
    COHERENT("Когерентный"),
    NONCOHERENT("Некогерентный"),
}

/**
 * Decision threshold γ of the receiver l ≷ γ (lecture 9).
 * For the coherent receiver γ is in units of the statistic l = (2/N₀)∫r(t)[s₁ − s₀]dt;
 * for the noncoherent one — in units of the envelope normalised to the noise RMS.
 */
@Serializable
sealed interface ThresholdMode {
    /** Ideal observer (Котельников): γ = (E₁ − E₀)/N₀ + ln(p₀/p₁). */
    @Serializable
    data object Optimal : ThresholdMode

    @Serializable
    data class Fixed(val gamma: Double) : ThresholdMode

    /** γ = c·E/N₀ — as in tasks "порог γ = E/(4N₀)". */
    @Serializable
    data class EnergyFactor(val factor: Double) : ThresholdMode

    /** Neyman–Pearson: γ is chosen from the false alarm probability α. */
    @Serializable
    data class NeymanPearson(val alpha: Double) : ThresholdMode
}

/** Energies are relative to the reference energy E: E₁ = k₁E, E₀ = k₀E; ρ — correlation coefficient. */
data class SignalPair(val k1: Double, val k0: Double, val rho: Double)

@Serializable
data class ReceptionParams(
    val coherence: Coherence = Coherence.COHERENT,
    val modulation: Modulation = Modulation.AT,
    /** q = 2E/N₀. */
    val q: Double = 10.0,
    val p0: Double = 0.5,
    val threshold: ThresholdMode = ThresholdMode.Optimal,
    /** Phase deviation for PSK, degrees (180° → antipodal signals, ρ = cos Δφ). */
    val pskDeviationDeg: Double = 180.0,
    /** ΔT = (ω₁ − ω₀)T for FSK; null → |ΔT| ≫ 1, ρ = 0. */
    val fskDeltaT: Double? = null,
    val customK1: Double = 1.0,
    val customK0: Double = 0.0,
    val customRho: Double = 0.0,
) {
    val p1: Double get() = 1.0 - p0

    fun signalPair(): SignalPair = when (modulation) {
        Modulation.AT -> SignalPair(k1 = 1.0, k0 = 0.0, rho = 0.0)
        Modulation.FSK -> SignalPair(k1 = 1.0, k0 = 1.0, rho = fskRho(fskDeltaT))
        Modulation.PSK -> SignalPair(k1 = 1.0, k0 = 1.0, rho = cos(pskDeviationDeg * PI / 180.0))
        Modulation.CUSTOM -> SignalPair(k1 = customK1, k0 = customK0, rho = customRho)
    }

    companion object {
        /** ρ = sin(ΔT)/ΔT (lecture 10). */
        fun fskRho(deltaT: Double?): Double = when {
            deltaT == null -> 0.0
            abs(deltaT) < 1e-9 -> 1.0
            else -> sin(deltaT) / deltaT
        }
    }
}
