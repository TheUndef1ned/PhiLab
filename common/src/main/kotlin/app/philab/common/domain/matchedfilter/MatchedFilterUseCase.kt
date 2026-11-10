package app.philab.common.domain.matchedfilter

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Mismatch losses of the optimal filter / correlator:
 *
 * 1. Matched filter for a rectangular pulse of duration T₀ receives a rectangular pulse of the same
 *    energy E₀ but duration T (practice 8.3). At t = T₀:
 *      q(T) = 2E₀/N₀ · min(T, T₀)²/(T·T₀)  → T/T₀ for T ≤ T₀, T₀/T for T ≥ T₀.
 * 2. Correlator with a reference phase error φ (practice 1.1): q(φ) = q_max·cos²φ.
 */
class MatchedFilterUseCase @Inject constructor() {

    enum class Mode { DURATION, PHASE }

    data class Point(val x: Double, val gain: Double)

    data class Result(
        val mode: Mode,
        val gain: Double,
        val qOut: Double,
        val lossDb: Double,
        /** Pe of coherent PSK with the degraded SNR (phase mode) — для иллюстрации. */
        val pskPe: Double?,
        val curve: List<Point>,
        val steps: List<String>,
    )

    fun duration(q0: Double, ratio: Double): kotlin.Result<Result> = runCatching {
        require(q0 > 0) { "2E₀/N₀ должно быть > 0" }
        require(ratio > 0) { "T/T₀ должно быть > 0" }
        val gain = durationGain(ratio)
        val qOut = q0 * gain
        val curve = (1..200).map { i ->
            val r = 0.05 + (4.0 - 0.05) * (i - 1) / 199.0
            Point(r, durationGain(r))
        }
        val steps = listOf(
            "Импульс: энергия E₀, длительность T = ${f(ratio)}·T₀, амплитуда U = √(E₀/T)",
            "Отклик СФ в момент T₀: y(T₀) = ∫ U·U₀ dt по перекрытию = U·U₀·min(T, T₀)",
            "Дисперсия шума: N₀/2·U₀²T₀ ⇒ q = 2E₀/N₀·min(T, T₀)²/(T·T₀)",
            "q/q_max = ${f(gain)} ⇒ q = ${f(q0)}·${f(gain)} = ${f(qOut)}",
        )
        Result(Mode.DURATION, gain, qOut, NormalDistribution.toDb(gain), null, curve, steps)
    }

    fun phase(qMax: Double, phiDeg: Double): kotlin.Result<Result> = runCatching {
        require(qMax > 0) { "2E/N₀ должно быть > 0" }
        val gain = phaseGain(phiDeg)
        val qOut = qMax * gain
        val c = cos(phiDeg * PI / 180.0)
        val pe = NormalDistribution.upperTail(sqrt(qMax) * c)
        val curve = (0..180).map { d -> Point(d.toDouble(), phaseGain(d.toDouble())) }
        val steps = listOf(
            "Выход коррелятора: ∫ A sin(ω₀t)·A sin(ω₀t + φ) dt = (A²T/2)·cos φ",
            "Сигнальная составляющая ∝ cos φ, шумовая не зависит от φ ⇒ q(φ) = q_max·cos²φ",
            "cos²(${f(phiDeg)}°) = ${f(gain)} ⇒ q = ${f(qOut)}",
            "Для ФТ с ошибкой фазы опоры: Pe = 1 − Φ(√(2E/N₀)·cos φ) = ${NumberFormatter.probability(pe)}",
        )
        Result(Mode.PHASE, gain, qOut, NormalDistribution.toDb(max(gain, 1e-300)), pe, curve, steps)
    }

    companion object {
        fun durationGain(ratio: Double): Double = min(ratio, 1.0) * min(ratio, 1.0) / ratio

        fun phaseGain(phiDeg: Double): Double {
            val c = cos(phiDeg * PI / 180.0)
            return c * c
        }
    }

    private fun f(v: Double) = NumberFormatter.format(v, 5)
}
