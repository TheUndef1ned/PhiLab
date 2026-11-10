package app.philab.common.domain.reception.usecase

import app.philab.common.domain.math.Bessel
import app.philab.common.domain.math.MarcumQ
import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.ReceptionResult
import app.philab.common.domain.reception.model.ThresholdMode
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Binary reception in white Gaussian noise (lectures 9–11, 13–15).
 *
 * Coherent receiver: l = (2/N₀)∫r(t)[s₁(t) − s₀(t)]dt ≷ γ, l ~ N(m₁, σ²) or N(m₀, σ²):
 *   m₁ = 2E₁/N₀·(1 − √(E₀/E₁)ρ),  m₀ = −2E₀/N₀·(1 − √(E₁/E₀)ρ),
 *   σ² = 2(E₁+E₀)/N₀ − 4√(E₁E₀)ρ/N₀,
 *   α = 1 − Φ((γ − m₀)/σ),  β = Φ((γ − m₁)/σ),  Pe = p₀α + p₁β.
 *
 * Noncoherent receiver: AT — envelope vs threshold (Rayleigh/Rice, Marcum Q),
 * FSK — comparison of two envelopes, Pe = ½·e^{−(2E/N₀)/4}.
 */
class CalculateReceptionUseCase @Inject constructor() {

    operator fun invoke(params: ReceptionParams): Result<ReceptionResult> = runCatching {
        validate(params)
        when (params.coherence) {
            Coherence.COHERENT -> coherent(params)
            Coherence.NONCOHERENT -> noncoherent(params)
        }
    }

    /** Fast path without solution steps — for plotting. */
    fun peOnly(params: ReceptionParams): Double? = invoke(params).getOrNull()?.pe

    private fun validate(params: ReceptionParams) {
        require(params.q >= 0.0) { "2E/N₀ должно быть ≥ 0" }
        require(params.p0 in 0.0..1.0) { "p₀ должно быть в [0; 1]" }
        (params.threshold as? ThresholdMode.NeymanPearson)?.let {
            require(it.alpha > 0.0 && it.alpha < 1.0) { "α должно быть в (0; 1)" }
        }
    }

    // ------------------------------------------------------------------ coherent

    private fun coherent(params: ReceptionParams): ReceptionResult {
        val (k1, k0, rho) = params.signalPair()
        require(k1 >= 0 && k0 >= 0) { "Энергии сигналов должны быть ≥ 0" }
        require(rho in -1.0..1.0) { "ρ должно быть в [−1; 1]" }
        val q = params.q
        val p0 = params.p0
        val p1 = params.p1
        val cross = sqrt(k0 * k1) * rho

        val m1 = q * (k1 - cross)
        val m0 = -q * (k0 - cross)
        val variance = q * (k1 + k0 - 2.0 * cross)
        require(variance > 1e-15) {
            if (q == 0.0) "При 2E/N₀ = 0 сигнал отсутствует: Pe = min(p₀, p₁)" else "Сигналы неразличимы: σ = 0"
        }
        val sigma = sqrt(variance)
        val gammaOpt = q * (k1 - k0) / 2.0 + lnRatio(p0, p1)

        val gamma = when (val t = params.threshold) {
            ThresholdMode.Optimal -> gammaOpt
            is ThresholdMode.Fixed -> t.gamma
            is ThresholdMode.EnergyFactor -> t.factor * q / 2.0
            is ThresholdMode.NeymanPearson -> m0 + sigma * NormalDistribution.inverseUpperTail(t.alpha)
        }

        val z0 = (gamma - m0) / sigma
        val z1 = (gamma - m1) / sigma
        val alpha = NormalDistribution.upperTail(z0)
        val beta = NormalDistribution.cdf(z1)
        val pe = p0 * alpha + p1 * beta

        val peOpt = if (params.threshold == ThresholdMode.Optimal) pe else {
            p0 * NormalDistribution.upperTail((gammaOpt - m0) / sigma) +
                p1 * NormalDistribution.cdf((gammaOpt - m1) / sigma)
        }
        val loss = NormalDistribution.toDb(4.0 / (k1 + k0 - 2.0 * cross))

        val steps = buildList {
            add("2E/N₀ = ${f(q)}, E/N₀ = ${f(q / 2)}; p₀ = ${f(p0)}, p₁ = ${f(p1)}")
            add("E₁ = ${f(k1)}·E, E₀ = ${f(k0)}·E, ρ = ${f(rho, 5)}" + rhoHint(params))
            add("m₁ = 2E₁/N₀·(1 − √(E₀/E₁)·ρ) = ${f(m1)}")
            add("m₀ = −2E₀/N₀·(1 − √(E₁/E₀)·ρ) = ${f(m0)}")
            add("σ² = 2(E₁+E₀)/N₀ − 4√(E₁E₀)·ρ/N₀ = ${f(variance)} ⇒ σ = ${f(sigma)}")
            add("γ_опт = (E₁ − E₀)/N₀ + ln(p₀/p₁) = ${f(gammaOpt)}")
            when (val t = params.threshold) {
                ThresholdMode.Optimal -> add("Порог оптимальный: γ = ${f(gamma)}")
                is ThresholdMode.Fixed -> add("Порог задан: γ = ${f(gamma)}")
                is ThresholdMode.EnergyFactor ->
                    add("γ = ${f(t.factor)}·E/N₀ = ${f(t.factor)}·${f(q / 2)} = ${f(gamma)}")
                is ThresholdMode.NeymanPearson -> {
                    add("Нейман–Пирсон: α = 1 − Φ((γ − m₀)/σ) = ${p(t.alpha)}")
                    add("Φ((γ − m₀)/σ) = 1 − α ⇒ (γ − m₀)/σ = ${f(z0, 5)} ⇒ γ = ${f(gamma)}")
                }
            }
            add("α = 1 − Φ((γ − m₀)/σ) = 1 − Φ(${f(z0, 4)}) = ${p(alpha)}")
            add("β = Φ((γ − m₁)/σ) = Φ(${f(z1, 4)}) = ${p(beta)}")
            add("Pe = p₀α + p₁β = ${f(p0)}·${p(alpha)} + ${f(p1)}·${p(beta)} = ${p(pe)}")
            closedForm(params, variance)?.let { add(it) }
        }

        val notes = buildList {
            if (params.threshold != ThresholdMode.Optimal) {
                add("С оптимальным порогом γ = ${f(gammaOpt)}: Pe = ${p(peOpt)}")
            }
            add("Проигрыш относительно ФТ (ρ = −1): ${NumberFormatter.db(loss)}")
            if (params.threshold is ThresholdMode.NeymanPearson) {
                add("P_D = 1 − β = ${p(1 - beta)}")
            }
        }

        return ReceptionResult(
            params = params,
            alpha = alpha,
            beta = beta,
            pe = pe,
            gamma = gamma,
            gammaOptimal = gammaOpt,
            peOptimal = peOpt,
            m0 = m0,
            m1 = m1,
            sigma = sigma,
            lossVsPskDb = loss,
            steps = steps,
            notes = notes,
        )
    }

    private fun rhoHint(params: ReceptionParams): String = when (params.modulation) {
        Modulation.PSK -> " (ρ = cos Δφ, Δφ = ${f(params.pskDeviationDeg)}°)"
        Modulation.FSK -> params.fskDeltaT?.let { " (ρ = sin ΔT/ΔT, ΔT = ${f(it)})" } ?: " (|ω₁ − ω₀|T ≫ 1)"
        else -> ""
    }

    /** Compact formula from lecture 10 for p₀ = p₁ = ½ and the optimal threshold. */
    private fun closedForm(params: ReceptionParams, variance: Double): String? {
        if (params.threshold != ThresholdMode.Optimal || params.p0 != 0.5) return null
        val arg = sqrt(variance) / 2.0
        val formula = when {
            params.modulation == Modulation.AT -> "Pe_АТ = 1 − Φ(√(E/2N₀))"
            params.modulation == Modulation.FSK && params.fskDeltaT == null -> "Pe_ЧТ = 1 − Φ(√(E/N₀))"
            params.modulation == Modulation.PSK && params.pskDeviationDeg == 180.0 -> "Pe_ФТ = 1 − Φ(√(2E/N₀))"
            else -> "Pe = 1 − Φ(√((E₁+E₀−2√(E₁E₀)ρ)/2N₀))"
        }
        return "Проверка: $formula = 1 − Φ(${f(arg, 5)}) = ${p(NormalDistribution.upperTail(arg))}"
    }

    // --------------------------------------------------------------- noncoherent

    private fun noncoherent(params: ReceptionParams): ReceptionResult = when (params.modulation) {
        Modulation.AT -> noncoherentAt(params)
        Modulation.FSK -> noncoherentFsk(params)
        Modulation.PSK -> throw IllegalArgumentException(
            "Оптимальный некогерентный приём ФТ невозможен: огибающие на выходах обоих СФ одинаковы " +
                "(лекция 15). Используйте ОФТ."
        )
        Modulation.CUSTOM -> throw IllegalArgumentException(
            "Для некогерентного приёма доступны АТ и ЧТ"
        )
    }

    private fun noncoherentAt(params: ReceptionParams): ReceptionResult {
        val q = params.q
        val v = sqrt(q)
        val p0 = params.p0
        val p1 = params.p1
        require(q > 0) { "2E/N₀ должно быть > 0" }

        val hOpt = Bessel.argLnI0(q / 2.0 + lnRatio(p0, p1)) / v
        val h = when (val t = params.threshold) {
            ThresholdMode.Optimal -> hOpt
            is ThresholdMode.Fixed -> t.gamma
            is ThresholdMode.EnergyFactor -> t.factor * q / 2.0
            is ThresholdMode.NeymanPearson -> sqrt(-2.0 * ln(t.alpha))
        }
        val alpha = MarcumQ.rayleighTail(h)
        val beta = MarcumQ.cdf(v, h)
        val pe = p0 * alpha + p1 * beta
        val peOpt = if (params.threshold == ThresholdMode.Optimal) pe else {
            p0 * MarcumQ.rayleighTail(hOpt) + p1 * MarcumQ.cdf(v, hOpt)
        }
        val approx = 0.5 * exp(-q / 8.0)

        val steps = buildList {
            add("2E/N₀ = ${f(q)}, √(2E/N₀) = ${f(v)}; p₀ = ${f(p0)}, p₁ = ${f(p1)}")
            add("Огибающая (нормированная на СКО шума): λ=0 — Релей, λ=1 — Райс с v = √(2E/N₀)")
            add("γ*_опт = Arg ln I₀(E/N₀ + ln(p₀/p₁)) / √(2E/N₀) = ${f(hOpt)}")
            when (val t = params.threshold) {
                ThresholdMode.Optimal -> add("Порог оптимальный: γ* = ${f(h)}")
                is ThresholdMode.NeymanPearson -> add("Нейман–Пирсон: α = e^{−γ*²/2} ⇒ γ* = √(−2 ln α) = ${f(h)} (α = ${p(t.alpha)})")
                else -> add("Порог задан: γ* = ${f(h)}")
            }
            add("α = e^{−γ*²/2} = ${p(alpha)}")
            add("β = 1 − Q(√(2E/N₀), γ*) = ${p(beta)}")
            add("Pe = p₀α + p₁β = ${p(pe)}")
            if (p0 == 0.5) {
                add("Pe_АТ = ½[1 + e^{−γ*²/2} − Q(√(2E/N₀), γ*)] (точная формула, лекция 14)")
            }
            add("При большом ОСШ: Pe_АТ ≈ ½·e^{−(2E/N₀)/8} = ${p(approx)}")
        }
        val notes = buildList {
            if (params.threshold != ThresholdMode.Optimal) add("С оптимальным порогом: Pe = ${p(peOpt)}")
            add("Q(v, u) — функция Маркума, считается численно")
        }
        return ReceptionResult(
            params = params,
            alpha = alpha,
            beta = beta,
            pe = pe,
            gamma = h,
            gammaOptimal = hOpt,
            peOptimal = peOpt,
            approximatePe = approx,
            steps = steps,
            notes = notes,
        )
    }

    private fun noncoherentFsk(params: ReceptionParams): ReceptionResult {
        val q = params.q
        val pe = 0.5 * exp(-q / 4.0)
        val steps = listOf(
            "2E/N₀ = ${f(q)}",
            "Сравнение огибающих на выходах двух СФ, порог 0; |ω₁ − ω₀|T ≫ 1",
            "Pe_ЧТ = ½·e^{−(2E/N₀)/4} = ½·e^{−${f(q / 4)}} = ${p(pe)}",
        )
        return ReceptionResult(
            params = params,
            alpha = pe,
            beta = pe,
            pe = pe,
            gamma = 0.0,
            gammaOptimal = 0.0,
            peOptimal = pe,
            steps = steps,
            notes = listOf("Ошибки симметричны: α = β, поэтому Pe не зависит от p₀ при нулевом пороге"),
        )
    }

    private fun lnRatio(p0: Double, p1: Double): Double = when {
        p0 <= 0.0 -> Double.NEGATIVE_INFINITY
        p1 <= 0.0 -> Double.POSITIVE_INFINITY
        else -> ln(p0 / p1)
    }

    private fun f(v: Double, sig: Int = 6) = NumberFormatter.format(v, sig)
    private fun p(v: Double) = NumberFormatter.probability(v)
}
