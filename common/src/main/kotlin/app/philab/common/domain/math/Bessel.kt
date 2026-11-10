package app.philab.common.domain.math

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Modified Bessel function of the first kind, order zero — I₀(x).
 * Appears in the Rice distribution and in the optimal noncoherent receiver (lectures 13–14).
 */
object Bessel {

    private const val SERIES_LIMIT = 50.0

    /** e^{-x}·I₀(x) for x ≥ 0 (never overflows). */
    fun i0Scaled(x: Double): Double {
        val ax = abs(x)
        if (ax <= SERIES_LIMIT) {
            // I₀(x) = Σ (x/2)^{2k} / (k!)²
            val q = ax * ax / 4.0
            var term = exp(-ax)
            var sum = term
            var k = 1
            while (k < 400) {
                term *= q / (k.toDouble() * k)
                sum += term
                if (term < 1e-17 * sum && k > ax) break
                k++
            }
            return sum
        }
        // Asymptotic series: I₀(x) ≈ eˣ/√(2πx) · Σ ((2k−1)!!)² / (k!·(8x)^k)
        var term = 1.0
        var sum = 1.0
        for (k in 1..12) {
            val odd = (2 * k - 1).toDouble()
            term *= odd * odd / (k * 8.0 * ax)
            sum += term
        }
        return sum / sqrt(2.0 * PI * ax)
    }

    /** ln I₀(x). */
    fun lnI0(x: Double): Double = abs(x) + ln(i0Scaled(x))

    /**
     * "Arg ln I₀" from lecture 14 — inverse of y = ln I₀(x) for x ≥ 0.
     * Returns 0 for y ≤ 0.
     */
    fun argLnI0(y: Double): Double {
        if (y <= 0.0) return 0.0
        if (y.isInfinite()) return Double.POSITIVE_INFINITY
        var lo = 0.0
        var hi = y + 2.0 * sqrt(y) + 10.0 // ln I₀(x) ≥ x − ln√(2πx)
        while (lnI0(hi) < y) hi *= 2.0
        repeat(200) {
            val mid = 0.5 * (lo + hi)
            if (lnI0(mid) < y) lo = mid else hi = mid
        }
        return 0.5 * (lo + hi)
    }
}
