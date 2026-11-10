package app.philab.common.domain.phi.usecase

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.phi.model.PhiLookupResult
import app.philab.common.domain.phi.model.PhiMethod
import app.philab.common.domain.phi.model.PhiTable
import app.philab.common.domain.phi.repository.PhiTableRepository
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Φ(z) "как по таблице":
 * 1. z < 0 → Φ(z) = 1 − Φ(|z|);
 * 2. |z| on the 0.01 grid → the cell value (O(1) index = round(z·100));
 * 3. between grid points → linear interpolation of the two neighbouring cells;
 * 4. |z| > 4.99 → analytic formula (the table ends there).
 */
class LookupPhiUseCase @Inject constructor(
    private val repository: PhiTableRepository,
) {

    suspend operator fun invoke(z: Double): Result<PhiLookupResult> =
        repository.getTable().map { table -> lookup(table, z) }

    companion object {

        private const val GRID_EPS = 1e-9

        fun lookup(table: PhiTable, z: Double): PhiLookupResult {
            val negative = z < 0
            val a = abs(z)
            val steps = mutableListOf<String>()
            if (negative) {
                steps += "z < 0 ⇒ Φ(${fmt(z)}) = 1 − Φ(${fmt(a)})"
            }

            val position = (a - table.zMin) / table.step
            val nearest = position.roundToInt()
            val positive: Triple<Double, PhiMethod, Pair<Int?, Int?>> = when {
                abs(position - nearest) < GRID_EPS && nearest in 0 until table.size -> {
                    val row = nearest / table.columns
                    val col = nearest % table.columns
                    steps += "Строка z = ${NumberFormatter.fixed(table.zAt(row * table.columns), 1)}, " +
                        "столбец ${NumberFormatter.fixed(col * table.step, 2)} ⇒ " +
                        "Φ(${NumberFormatter.fixed(a, 2)}) = " +
                        NumberFormatter.tableNotation(table.valueAt(nearest), table.decimalsAt(nearest))
                    Triple(table.valueAt(nearest), PhiMethod.TABLE, nearest to table.decimalsAt(nearest))
                }

                position >= 0 && position < table.size - 1 -> {
                    val lo = floor(position).toInt()
                    val hi = lo + 1
                    val t = position - lo
                    val v = table.valueAt(lo) + t * (table.valueAt(hi) - table.valueAt(lo))
                    steps += "Соседние ячейки: Φ(${NumberFormatter.fixed(table.zAt(lo), 2)}) = " +
                        "${NumberFormatter.tableNotation(table.valueAt(lo), table.decimalsAt(lo))}, " +
                        "Φ(${NumberFormatter.fixed(table.zAt(hi), 2)}) = " +
                        NumberFormatter.tableNotation(table.valueAt(hi), table.decimalsAt(hi))
                    steps += "Интерполяция: Φ ≈ Φ₁ + (z − z₁)/0,01·(Φ₂ − Φ₁) = ${fmt(v, 8)}"
                    Triple(v, PhiMethod.INTERPOLATED, lo to maxOf(table.decimalsAt(lo), table.decimalsAt(hi)))
                }

                else -> {
                    val v = NormalDistribution.cdf(a)
                    steps += "z = ${fmt(a)} > ${NumberFormatter.fixed(table.zMax, 2)} — за пределами таблицы, " +
                        "1 − Φ(z) = ${NumberFormatter.scientific(NormalDistribution.upperTail(a))}"
                    Triple(v, PhiMethod.ANALYTIC, null to null)
                }
            }

            val (phiAbs, method, cell) = positive
            val result = if (negative) 1.0 - phiAbs else phiAbs
            if (negative) {
                steps += "Φ(${fmt(z)}) = 1 − ${fmt(phiAbs, 8)} = ${fmt(result, 8)}"
            }
            return PhiLookupResult(
                z = z,
                phi = result,
                method = method,
                usedSymmetry = negative,
                cellIndex = cell.first,
                cellDecimals = cell.second,
                exact = NormalDistribution.cdf(z),
                steps = steps,
            )
        }

        private fun fmt(v: Double, sig: Int = 6) = NumberFormatter.format(v, sig)
    }
}
