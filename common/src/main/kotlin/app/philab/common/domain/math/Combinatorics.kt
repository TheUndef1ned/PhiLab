package app.philab.common.domain.math

import kotlin.math.exp
import kotlin.math.ln

object Combinatorics {

    fun lnFactorial(n: Int): Double {
        var s = 0.0
        for (i in 2..n) s += ln(i.toDouble())
        return s
    }

    /** C(n, k) as Double. */
    fun binomialCoefficient(n: Int, k: Int): Double {
        if (k < 0 || k > n) return 0.0
        return exp(lnFactorial(n) - lnFactorial(k) - lnFactorial(n - k))
    }

    /** P(exactly k errors among n independent symbols with error probability p). */
    fun binomialPmf(n: Int, k: Int, p: Double): Double {
        if (k < 0 || k > n) return 0.0
        if (p <= 0.0) return if (k == 0) 1.0 else 0.0
        if (p >= 1.0) return if (k == n) 1.0 else 0.0
        val lnP = lnFactorial(n) - lnFactorial(k) - lnFactorial(n - k) +
            k * ln(p) + (n - k) * ln(1.0 - p)
        return exp(lnP)
    }

    /** P(no more than k errors). */
    fun binomialCdf(n: Int, k: Int, p: Double): Double =
        (0..minOf(k, n)).sumOf { binomialPmf(n, it, p) }.coerceIn(0.0, 1.0)
}
