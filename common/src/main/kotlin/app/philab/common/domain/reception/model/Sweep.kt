package app.philab.common.domain.reception.model

import kotlinx.serialization.Serializable

/** Variable on the X axis for "Построить зависимость Pe(…)". */
@Serializable
enum class SweepVariable(val title: String, val axis: String) {
    Q("2E/N₀", "2E/N₀"),
    Q_DB("2E/N₀ в дБ", "2E/N₀, дБ"),
    GAMMA("Порог γ", "γ"),
    P0("Априорная p₀", "p₀"),
    PSK_PHASE("Девиация фазы ФТ", "Δφ, °"),
    FSK_DELTA_T("Расстройка ЧТ ΔT", "ΔT"),
}

/** Value on the Y axis. */
@Serializable
enum class SweepOutput(val title: String) {
    PE("Pe"),
    ALPHA("α"),
    BETA("β"),
}

data class SweepPoint(val x: Double, val y: Double)

data class SweepSeries(val name: String, val points: List<SweepPoint>)

data class SweepResult(
    val variable: SweepVariable,
    val output: SweepOutput,
    val series: List<SweepSeries>,
)

@Serializable
data class SweepRequest(
    val base: ReceptionParams,
    val variable: SweepVariable,
    val from: Double,
    val to: Double,
    val points: Int = 121,
    val output: SweepOutput = SweepOutput.PE,
    /** Adds the curve with the optimal threshold ("сравнить с оптимальным выбором порога"). */
    val compareWithOptimal: Boolean = true,
) {
    companion object {
        fun defaultRange(variable: SweepVariable, base: ReceptionParams): Pair<Double, Double> = when (variable) {
            SweepVariable.Q -> 0.0 to 30.0
            SweepVariable.Q_DB -> -5.0 to 15.0
            SweepVariable.GAMMA -> if (base.coherence == Coherence.COHERENT) 0.0 to base.q else 0.0 to 2.0 * kotlin.math.sqrt(base.q)
            SweepVariable.P0 -> 0.0 to 1.0
            SweepVariable.PSK_PHASE -> 0.0 to 360.0
            SweepVariable.FSK_DELTA_T -> 0.0 to 20.0
        }
    }
}
