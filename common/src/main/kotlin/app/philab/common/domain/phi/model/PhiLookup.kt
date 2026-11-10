package app.philab.common.domain.phi.model

/** How the value was obtained — shown to the user so the answer can be written down "by the book". */
enum class PhiMethod(val label: String) {
    TABLE("прямо из таблицы"),
    INTERPOLATED("линейная интерполяция между ячейками"),
    ANALYTIC("вне таблицы (z ≥ 5) — аналитически"),
}

data class PhiLookupResult(
    val z: Double,
    val phi: Double,
    val method: PhiMethod,
    /** Φ(−z) = 1 − Φ(z) was applied. */
    val usedSymmetry: Boolean,
    /** Table index of the cell for |z| (lower neighbour when interpolating), null outside the table. */
    val cellIndex: Int?,
    /** Printed precision of the cell (number of decimals), null when not from the table. */
    val cellDecimals: Int?,
    /** Reference value from the analytic formula, for comparison. */
    val exact: Double,
    val steps: List<String>,
)

data class InversePhiResult(
    val phi: Double,
    val z: Double,
    val lowerIndex: Int?,
    val upperIndex: Int?,
    val exactZ: Double,
    val steps: List<String>,
)
