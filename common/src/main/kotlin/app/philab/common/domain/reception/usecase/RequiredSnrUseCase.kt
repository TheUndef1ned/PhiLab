package app.philab.common.domain.reception.usecase

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/**
 * "Какое требуется 2E/N₀ для Pe = …" (practice 6): equal priors, optimal threshold.
 * Coherent cases are solved through Φ⁻¹, noncoherent FSK in closed form,
 * noncoherent AT — numerically (bisection over the exact Marcum-Q formula).
 */
class RequiredSnrUseCase @Inject constructor(
    private val calculate: CalculateReceptionUseCase,
) {

    data class Row(
        val title: String,
        val q: Double,
        val formula: String,
    ) {
        val db: Double get() = NormalDistribution.toDb(q)
    }

    data class Comparison(val title: String, val deltaDb: Double)

    data class Result(
        val pe: Double,
        val z: Double,
        val rows: List<Row>,
        val comparisons: List<Comparison>,
        val steps: List<String>,
    )

    operator fun invoke(pe: Double, pskDeviationDeg: Double = 180.0): kotlin.Result<Result> = runCatching {
        require(pe > 0.0 && pe < 0.5) { "Pe должна быть в (0; 0,5)" }
        val z = NormalDistribution.inverseUpperTail(pe)

        val atCoh = Row("АТ, когерентный", 4.0 * z * z, "1 − Φ(√(E/2N₀)) = Pe ⇒ 2E/N₀ = 4z²")
        val fskCoh = Row("ЧТ, когерентный", 2.0 * z * z, "1 − Φ(√(E/N₀)) = Pe ⇒ 2E/N₀ = 2z²")
        val pskCoh = Row("ФТ, когерентный", z * z, "1 − Φ(√(2E/N₀)) = Pe ⇒ 2E/N₀ = z²")
        val fskNon = Row("ЧТ, некогерентный", -4.0 * ln(2.0 * pe), "½e^{−q/4} = Pe ⇒ q = −4 ln(2Pe)")
        val atNonApprox = Row("АТ, некогерентный (≈)", -8.0 * ln(2.0 * pe), "½e^{−q/8} = Pe ⇒ q = −8 ln(2Pe)")
        val atNonExact = Row(
            "АТ, некогерентный (точно)",
            solveNoncoherentAt(pe),
            "½[1 + e^{−γ*²/2} − Q(√q, γ*)] = Pe, численно",
        )

        val rows = mutableListOf(atCoh, fskCoh, pskCoh, atNonExact, atNonApprox, fskNon)
        if (pskDeviationDeg != 180.0) {
            val rho = cos(pskDeviationDeg * PI / 180.0)
            // Pe = 1 − Φ(√(q(1 − ρ)/2))
            rows += Row(
                "ФТ, Δφ = ${NumberFormatter.format(pskDeviationDeg)}°, когерентный",
                2.0 * z * z / (1.0 - rho),
                "1 − Φ(√(2E/N₀·(1 − ρ)/2)) = Pe, ρ = cos Δφ",
            )
        }

        val comparisons = listOf(
            Comparison("АТ: некогерентный − когерентный", atNonExact.db - atCoh.db),
            Comparison("ЧТ: некогерентный − когерентный", fskNon.db - fskCoh.db),
            Comparison("АТ − ЧТ (когерентный)", atCoh.db - fskCoh.db),
            Comparison("ЧТ − ФТ (когерентный)", fskCoh.db - pskCoh.db),
        )

        val steps = listOf(
            "1 − Φ(z) = Pe ⇒ Φ(z) = ${NumberFormatter.format(1 - pe, 8)} ⇒ z = ${NumberFormatter.format(z, 5)}",
            "Разница в децибелах: Δ = 10·lg(q₂/q₁)",
        )
        Result(pe = pe, z = z, rows = rows, comparisons = comparisons, steps = steps)
    }

    private fun solveNoncoherentAt(pe: Double): Double {
        var lo = log10(1e-3)
        var hi = log10(1e5)
        repeat(70) {
            val mid = 0.5 * (lo + hi)
            val value = calculate.peOnly(
                ReceptionParams(
                    coherence = Coherence.NONCOHERENT,
                    modulation = Modulation.AT,
                    q = 10.0.pow(mid),
                    p0 = 0.5,
                )
            ) ?: 0.5
            if (value > pe) lo = mid else hi = mid
        }
        return 10.0.pow(0.5 * (lo + hi))
    }
}
