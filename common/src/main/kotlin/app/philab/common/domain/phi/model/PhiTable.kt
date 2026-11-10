package app.philab.common.domain.phi.model

/**
 * The printed table of Φ(z) (Hald, Приложение IV) for z = zMin + i·step, i ∈ [0, size).
 *
 * [values] gives O(1) access by index; [decimals] keeps the printed precision of every cell
 * so the value can be shown exactly as in the book (e.g. 0,9³8215).
 */
class PhiTable(
    val title: String,
    val source: String,
    val zMin: Double,
    val step: Double,
    private val values: DoubleArray,
    private val decimals: IntArray,
) {
    init {
        require(values.size == decimals.size) { "values and decimals must have the same size" }
        require(values.isNotEmpty()) { "table must not be empty" }
    }

    val size: Int get() = values.size

    val zMax: Double get() = zMin + (size - 1) * step

    fun valueAt(index: Int): Double = values[index]

    fun decimalsAt(index: Int): Int = decimals[index]

    fun zAt(index: Int): Double = zMin + index * step

    /** Number of rows/columns in the book layout (rows by 0.1, columns by 0.01). */
    val columns: Int get() = 10
    val rows: Int get() = (size + columns - 1) / columns

    fun cell(row: Int, column: Int): Int = row * columns + column

    /**
     * Binary search over the monotone table: index of the last cell with value ≤ [phi], or -1.
     */
    fun floorIndexOf(phi: Double): Int {
        var lo = 0
        var hi = size - 1
        var ans = -1
        while (lo <= hi) {
            val mid = (lo + hi) ushr 1
            if (values[mid] <= phi) {
                ans = mid
                lo = mid + 1
            } else {
                hi = mid - 1
            }
        }
        return ans
    }
}
