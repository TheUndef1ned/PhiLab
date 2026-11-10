package app.philab.common.domain.math

import kotlin.math.exp
import kotlin.math.max

/**
 * Generalized Marcum Q-function of order 1 (lecture 14):
 *
 * Q(v, u) = ∫_u^∞ x·e^{−(x²+v²)/2}·I₀(xv) dx
 *
 * i.e. the survival function of the Rice distribution with parameter v.
 * Both tails are integrated directly (Simpson) so that tiny probabilities keep their relative precision.
 */
object MarcumQ {

    private const val TAIL_SPAN = 14.0
    private const val INTERVALS = 2000

    /** Q(v, u) = P(R > u). */
    fun q(v: Double, u: Double): Double {
        if (u <= 0.0) return 1.0
        if (u.isInfinite()) return 0.0
        val upper = max(u, v) + TAIL_SPAN
        return simpson(u, upper) { riceDensity(v, it) }.coerceIn(0.0, 1.0)
    }

    /** 1 − Q(v, u) = P(R ≤ u). */
    fun cdf(v: Double, u: Double): Double {
        if (u <= 0.0) return 0.0
        if (u.isInfinite()) return 1.0
        if (u > v + TAIL_SPAN) return 1.0 - q(v, u)
        return simpson(0.0, u) { riceDensity(v, it) }.coerceIn(0.0, 1.0)
    }

    /** w(x) = x·e^{−(x²+v²)/2}·I₀(xv), written through the scaled Bessel function. */
    fun riceDensity(v: Double, x: Double): Double {
        if (x <= 0.0) return 0.0
        val d = x - v
        return x * exp(-0.5 * d * d) * Bessel.i0Scaled(x * v)
    }

    /** Rayleigh tail: P(R > u | no signal) = e^{−u²/2}. */
    fun rayleighTail(u: Double): Double = if (u <= 0.0) 1.0 else exp(-0.5 * u * u)

    private inline fun simpson(a: Double, b: Double, f: (Double) -> Double): Double {
        if (b <= a) return 0.0
        val n = INTERVALS
        val h = (b - a) / n
        var sum = f(a) + f(b)
        for (i in 1 until n) {
            val x = a + i * h
            sum += if (i % 2 == 1) 4.0 * f(x) else 2.0 * f(x)
        }
        return sum * h / 3.0
    }
}
