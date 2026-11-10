package app.philab.common.domain.phi.usecase

import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.phi.model.InversePhiResult
import app.philab.common.domain.phi.model.PhiTable
import app.philab.common.domain.phi.repository.PhiTableRepository
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject

/**
 * Reverse lookup z = Φ⁻¹(p): binary search over the monotone table + linear interpolation.
 * Used e.g. for the Neyman–Pearson threshold (α → z).
 */
class InversePhiUseCase @Inject constructor(
    private val repository: PhiTableRepository,
) {

    suspend operator fun invoke(p: Double): Result<InversePhiResult> =
        repository.getTable().mapCatching { table -> inverse(table, p) }

    companion object {

        fun inverse(table: PhiTable, p: Double): InversePhiResult {
            require(p > 0.0 && p < 1.0) { "Φ должно быть в интервале (0; 1)" }
            val steps = mutableListOf<String>()
            val symmetric = p < 0.5
            val target = if (symmetric) 1.0 - p else p
            if (symmetric) {
                steps += "Φ < 0,5 ⇒ z < 0: ищем z' с Φ(z') = 1 − ${fmt(p)} = ${fmt(target)}, затем z = −z'"
            }

            val lo = table.floorIndexOf(target)
            val zPositive: Double
            var lower: Int? = null
            var upper: Int? = null
            if (lo >= 0 && lo < table.size - 1) {
                val hi = lo + 1
                val v1 = table.valueAt(lo)
                val v2 = table.valueAt(hi)
                lower = lo
                upper = hi
                zPositive = if (v2 == v1) {
                    table.zAt(lo)
                } else {
                    table.zAt(lo) + (target - v1) / (v2 - v1) * table.step
                }
                steps += "В таблице: Φ(${NumberFormatter.fixed(table.zAt(lo), 2)}) = " +
                    "${NumberFormatter.tableNotation(v1, table.decimalsAt(lo))} ≤ ${fmt(target)} ≤ " +
                    "${NumberFormatter.tableNotation(v2, table.decimalsAt(hi))} = Φ(${NumberFormatter.fixed(table.zAt(hi), 2)})"
                steps += "Интерполяция: z ≈ ${fmt(zPositive, 5)}"
            } else {
                zPositive = NormalDistribution.inverseUpperTail(1.0 - target)
                steps += "Значение вне таблицы — решаем аналитически: z ≈ ${fmt(zPositive, 5)}"
            }
            val z = if (symmetric) -zPositive else zPositive
            if (symmetric) steps += "z = −${fmt(zPositive, 5)}"
            return InversePhiResult(
                phi = p,
                z = z,
                lowerIndex = lower,
                upperIndex = upper,
                exactZ = NormalDistribution.quantile(p),
                steps = steps,
            )
        }

        private fun fmt(v: Double, sig: Int = 7) = NumberFormatter.format(v, sig)
    }
}
