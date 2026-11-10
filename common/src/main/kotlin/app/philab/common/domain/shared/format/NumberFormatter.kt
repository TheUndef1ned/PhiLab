package app.philab.common.domain.shared.format

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Locale
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/** Russian-style number formatting (decimal comma) used in answers and solution steps. */
object NumberFormatter {

    private const val SUPERSCRIPTS = "⁰¹²³⁴⁵⁶⁷⁸⁹"

    /** Up to [significant] significant digits, scientific notation for very small/large values. */
    fun format(value: Double, significant: Int = 6): String {
        if (value.isNaN()) return "—"
        if (value.isInfinite()) return if (value > 0) "∞" else "−∞"
        if (value == 0.0) return "0"
        val a = abs(value)
        if (a >= 1e-4 && a < 1e7) {
            val bd = BigDecimal(value).round(MathContext(significant, RoundingMode.HALF_EVEN))
            return bd.stripTrailingZeros().toPlainString().toRu()
        }
        return scientific(value, significant)
    }

    fun scientific(value: Double, significant: Int = 4): String {
        if (value == 0.0) return "0"
        var exp = floor(log10(abs(value))).toInt()
        var mantissa = value / 10.0.pow(exp)
        val rounded = BigDecimal(mantissa).round(MathContext(significant, RoundingMode.HALF_EVEN))
        mantissa = rounded.toDouble()
        if (abs(mantissa) >= 10.0) {
            mantissa /= 10.0
            exp += 1
        }
        val m = BigDecimal(mantissa).round(MathContext(significant, RoundingMode.HALF_EVEN))
            .stripTrailingZeros().toPlainString().toRu()
        return "$m·10${superscript(exp)}"
    }

    fun fixed(value: Double, decimals: Int): String =
        String.format(Locale.US, "%.${decimals}f", value).toRu()

    /** Probability: plain for ≥ 1e-4, otherwise scientific. */
    fun probability(value: Double): String =
        if (value >= 1e-4 || value == 0.0) format(value, 5) else scientific(value, 4)

    fun db(value: Double): String = "${fixed(value, 2)} дБ"

    /**
     * Book notation of the table: 0,9998215 → "0,9³8215" (superscript = number of leading nines).
     * One leading nine is printed plainly, as in the table (0,98928).
     */
    fun tableNotation(value: Double, decimals: Int): String {
        val plain = String.format(Locale.US, "%.${decimals}f", value)
        val dot = plain.indexOf('.')
        if (dot < 0 || !plain.startsWith("0.")) return plain.toRu()
        val frac = plain.substring(dot + 1)
        val nines = frac.takeWhile { it == '9' }.length
        return if (nines >= 2 && nines < frac.length) {
            "0,9${superscript(nines)}${frac.substring(nines)}"
        } else {
            "0,$frac"
        }
    }

    fun superscript(n: Int): String {
        val s = n.toString()
        return buildString {
            s.forEach { c ->
                when (c) {
                    '-' -> append('⁻')
                    else -> append(SUPERSCRIPTS[c - '0'])
                }
            }
        }
    }

    private fun String.toRu(): String = replace('.', ',').replace("-", "−")
}
