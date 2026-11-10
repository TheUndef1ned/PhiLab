package app.philab.common.domain.snr

import app.philab.common.domain.math.NormalDistribution
import kotlinx.serialization.Serializable
import kotlin.math.sqrt

/**
 * How a task states the signal-to-noise ratio. Lectures mostly use 2E/N₀ (q),
 * some practice tasks give E/N₀ (7.3, 8.2) or the amplitude ratio √(2E/N₀) (curves in lecture 10).
 */
@Serializable
enum class SnrUnit(val label: String) {
    TWO_E_N0("2E/N₀"),
    E_N0("E/N₀"),
    SQRT_TWO_E_N0("√(2E/N₀)"),
    DB_TWO_E_N0("2E/N₀, дБ"),
}

/** Value object: SNR is always stored as q = 2E/N₀. */
@JvmInline
value class Snr(val q: Double) {

    val eOverN0: Double get() = q / 2.0
    val amplitude: Double get() = sqrt(q)
    val db: Double get() = NormalDistribution.toDb(q)

    fun valueIn(unit: SnrUnit): Double = when (unit) {
        SnrUnit.TWO_E_N0 -> q
        SnrUnit.E_N0 -> eOverN0
        SnrUnit.SQRT_TWO_E_N0 -> amplitude
        SnrUnit.DB_TWO_E_N0 -> db
    }

    companion object {
        fun of(value: Double, unit: SnrUnit): Snr = Snr(
            when (unit) {
                SnrUnit.TWO_E_N0 -> value
                SnrUnit.E_N0 -> 2.0 * value
                SnrUnit.SQRT_TWO_E_N0 -> value * value
                SnrUnit.DB_TWO_E_N0 -> NormalDistribution.fromDb(value)
            }
        )
    }
}
