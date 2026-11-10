package app.philab.common.domain.math

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/**
 * Standard normal distribution N(0, 1).
 *
 * Φ(z) = 1/√(2π) ∫_{-∞}^{z} exp(-x²/2) dx — the "интеграл вероятности" from lecture 10.
 *
 * The analytic implementation is used outside of the printed table range (|z| ≥ 5) and
 * as the precise reference for every calculator; the table itself lives in assets/phi_table.json.
 */
object NormalDistribution {

    private const val LN_SQRT_2PI = 0.91893853320467274178
    private const val SERIES_LIMIT = 3.0
    private const val CONTINUED_FRACTION_DEPTH = 300

    /** Density φ(z). */
    fun pdf(z: Double): Double = exp(-0.5 * z * z - LN_SQRT_2PI)

    /** Φ(z). */
    fun cdf(z: Double): Double = when {
        z.isNaN() -> Double.NaN
        z < 0 -> upperTail(-z)
        else -> 1.0 - upperTail(z)
    }

    /**
     * Q(z) = 1 − Φ(z), computed without cancellation, so it keeps relative precision
     * deep in the tail (needed for Pe ~ 1e-10 and below).
     */
    fun upperTail(z: Double): Double {
        if (z.isNaN()) return Double.NaN
        if (z == Double.POSITIVE_INFINITY) return 0.0
        if (z == Double.NEGATIVE_INFINITY) return 1.0
        if (z < 0) return 1.0 - upperTail(-z)
        if (z < SERIES_LIMIT) {
            // Marsaglia: Φ(z) = 1/2 + φ(z)·(z + z³/3 + z⁵/(3·5) + …)
            val z2 = z * z
            var term = z
            var sum = z
            var i = 1
            while (abs(term) > 1e-17 * abs(sum) && i < 500) {
                term *= z2 / (2 * i + 1)
                sum += term
                i++
            }
            return 0.5 - sum * pdf(z)
        }
        // Q(z) = φ(z) / (z + 1/(z + 2/(z + 3/(z + …))))
        var t = z
        for (k in CONTINUED_FRACTION_DEPTH downTo 1) {
            t = z + k / t
        }
        return pdf(z) / t
    }

    /** Solves Q(z) = q for z (monotone bisection, precise in the tails). */
    fun inverseUpperTail(q: Double): Double {
        require(q > 0.0 && q < 1.0) { "q must be in (0, 1)" }
        var lo = -40.0
        var hi = 40.0
        repeat(200) {
            val mid = 0.5 * (lo + hi)
            if (upperTail(mid) > q) lo = mid else hi = mid
        }
        return 0.5 * (lo + hi)
    }

    /** Φ⁻¹(p). */
    fun quantile(p: Double): Double {
        require(p > 0.0 && p < 1.0) { "p must be in (0, 1)" }
        return if (p < 0.5) -inverseUpperTail(p) else inverseUpperTail(1.0 - p)
    }

    /**
     * Approximation by L.A. Osipov (lecture 10), valid for z ≥ 0, error < 0.2 %:
     * Φ(z) ≈ 1 − ½·10^(−z(0.34 + 0.16z)).
     */
    fun osipovApproximation(z: Double): Double {
        require(z >= 0) { "Osipov approximation is defined for z ≥ 0" }
        return 1.0 - 0.5 * 10.0.pow(-z * (0.34 + 0.16 * z))
    }

    fun toDb(ratio: Double): Double = 10.0 * log10(ratio)

    fun fromDb(db: Double): Double = 10.0.pow(db / 10.0)

    /** ln(Q(z)) without underflow for very large z. */
    fun lnUpperTail(z: Double): Double {
        val q = upperTail(z)
        return if (q > 0) ln(q) else -0.5 * z * z - LN_SQRT_2PI - ln(z)
    }
}
