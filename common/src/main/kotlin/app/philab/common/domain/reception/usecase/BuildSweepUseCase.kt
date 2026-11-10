package app.philab.common.domain.reception.usecase

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.ReceptionResult
import app.philab.common.domain.reception.model.SweepOutput
import app.philab.common.domain.reception.model.SweepPoint
import app.philab.common.domain.reception.model.SweepRequest
import app.philab.common.domain.reception.model.SweepResult
import app.philab.common.domain.reception.model.SweepSeries
import app.philab.common.domain.reception.model.SweepVariable
import app.philab.common.domain.reception.model.ThresholdMode
import javax.inject.Inject

/** Builds Pe(x) curves by re-running [CalculateReceptionUseCase] over a grid of x. */
class BuildSweepUseCase @Inject constructor(
    private val calculate: CalculateReceptionUseCase,
) {

    operator fun invoke(request: SweepRequest): Result<SweepResult> = runCatching {
        require(request.points in 2..2000) { "Число точек должно быть от 2 до 2000" }
        require(request.to > request.from) { "Конец диапазона должен быть больше начала" }
        validateVariable(request)

        val xs = List(request.points) { i ->
            request.from + (request.to - request.from) * i / (request.points - 1)
        }
        val main = SweepSeries(
            name = seriesName(request.base),
            points = xs.mapNotNull { x -> point(request, request.base, x) },
        )
        val series = mutableListOf(main)
        val canCompare = request.compareWithOptimal &&
            request.variable != SweepVariable.GAMMA &&
            request.base.threshold != ThresholdMode.Optimal
        if (canCompare) {
            val optimal = request.base.copy(threshold = ThresholdMode.Optimal)
            series += SweepSeries(
                name = "оптимальный порог",
                points = xs.mapNotNull { x -> point(request, optimal, x) },
            )
        }
        SweepResult(request.variable, request.output, series)
    }

    private fun validateVariable(request: SweepRequest) {
        when (request.variable) {
            SweepVariable.PSK_PHASE -> require(request.base.modulation == Modulation.PSK) {
                "Зависимость от Δφ строится для ФТ"
            }
            SweepVariable.FSK_DELTA_T -> require(request.base.modulation == Modulation.FSK) {
                "Зависимость от ΔT строится для ЧТ"
            }
            SweepVariable.P0 -> require(request.from >= 0.0 && request.to <= 1.0) { "p₀ ∈ [0; 1]" }
            else -> Unit
        }
    }

    private fun point(request: SweepRequest, base: ReceptionParams, x: Double): SweepPoint? {
        val params = when (request.variable) {
            SweepVariable.Q -> base.copy(q = x)
            SweepVariable.Q_DB -> base.copy(q = NormalDistribution.fromDb(x))
            SweepVariable.GAMMA -> base.copy(threshold = ThresholdMode.Fixed(x))
            SweepVariable.P0 -> base.copy(p0 = x)
            SweepVariable.PSK_PHASE -> base.copy(pskDeviationDeg = x)
            SweepVariable.FSK_DELTA_T -> base.copy(fskDeltaT = x)
        }
        val result: ReceptionResult = calculate(params).getOrNull() ?: return null
        val y = when (request.output) {
            SweepOutput.PE -> result.pe
            SweepOutput.ALPHA -> result.alpha
            SweepOutput.BETA -> result.beta
        }
        return if (y.isFinite()) SweepPoint(x, y) else null
    }

    private fun seriesName(params: ReceptionParams): String {
        val threshold = when (val t = params.threshold) {
            ThresholdMode.Optimal -> "γ опт."
            is ThresholdMode.Fixed -> "γ = ${t.gamma}"
            is ThresholdMode.EnergyFactor -> "γ = ${t.factor}·E/N₀"
            is ThresholdMode.NeymanPearson -> "α = ${t.alpha}"
        }
        return "${params.modulation.short}, ${params.coherence.title.lowercase()}, $threshold"
    }
}
