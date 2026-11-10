package app.philab.common.domain.normal

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.sqrt

/** Probabilities for X ~ N(m, D) through Φ (practice 9, tasks 1–2). */
class NormalProbabilityUseCase @Inject constructor() {

    data class Params(
        val mean: Double,
        /** Variance D = σ². */
        val variance: Double,
        val a: Double,
        /** Optional upper bound for P(a < X < b). */
        val b: Double? = null,
    )

    data class Result(
        val sigma: Double,
        val za: Double,
        val zb: Double?,
        /** P(X > a) = 1 − Φ((a − m)/σ). */
        val greater: Double,
        /** P(X < a) = Φ((a − m)/σ). */
        val less: Double,
        /** P(a < X < b) = Φ(z_b) − Φ(z_a). */
        val between: Double?,
        val steps: List<String>,
    )

    operator fun invoke(params: Params): Result {
        require(params.variance > 0) { "Дисперсия должна быть > 0" }
        val sigma = sqrt(params.variance)
        val za = (params.a - params.mean) / sigma
        val phiA = NormalDistribution.cdf(za)
        val greater = NormalDistribution.upperTail(za)
        val steps = mutableListOf(
            "σ = √D = √${f(params.variance)} = ${f(sigma)}",
            "z = (a − m)/σ = (${f(params.a)} − ${f(params.mean)})/${f(sigma)} = ${f(za, 5)}",
            "Φ(${f(za, 4)}) = ${f(phiA, 5)}",
            "P(X > a) = 1 − Φ(z) = ${NumberFormatter.probability(greater)}",
            "P(X < a) = Φ(z) = ${NumberFormatter.probability(phiA)}",
        )
        var zb: Double? = null
        var between: Double? = null
        params.b?.let { b ->
            val z2 = (b - params.mean) / sigma
            zb = z2
            between = (NormalDistribution.cdf(z2) - phiA).coerceAtLeast(0.0)
            steps += "z_b = (b − m)/σ = ${f(z2, 5)}, Φ(z_b) = ${f(NormalDistribution.cdf(z2), 5)}"
            steps += "P(a < X < b) = Φ(z_b) − Φ(z_a) = ${NumberFormatter.probability(between!!)}"
        }
        return Result(
            sigma = sigma,
            za = za,
            zb = zb,
            greater = greater,
            less = phiA,
            between = between,
            steps = steps,
        )
    }

    private fun f(v: Double, sig: Int = 6) = NumberFormatter.format(v, sig)
}
