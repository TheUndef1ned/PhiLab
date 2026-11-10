package app.philab.common.domain.dpsk

import javax.inject.Inject

/**
 * ОФТ (relative phase telegraphy, lecture 15): λ = 1 keeps the phase of the previous element,
 * λ = 0 (or −1) shifts it by π. The first element is a reference with an arbitrary phase.
 *
 * Phases are represented as multiples of π: 0 → 0, 1 → π.
 */
class DpskCodecUseCase @Inject constructor() {

    /** Alphabet the user writes symbols in. */
    enum class Alphabet(val one: String, val zero: String) {
        BINARY("1", "0"),
        BIPOLAR("1", "−1"),
    }

    data class Encoded(
        /** Phases including the reference element first. */
        val phases: List<Int>,
        val steps: List<String>,
    )

    data class Decoded(
        val symbols: List<Boolean>,
        val steps: List<String>,
    )

    /** [symbols]: true — λ = 1 (keep phase), false — λ = 0/−1 (shift by π). */
    fun encode(symbols: List<Boolean>, referencePhase: Int, alphabet: Alphabet): Result<Encoded> = runCatching {
        require(symbols.isNotEmpty()) { "Введите последовательность символов" }
        require(referencePhase == 0 || referencePhase == 1) { "Опорная фаза: 0 или π" }
        val phases = ArrayList<Int>(symbols.size + 1)
        phases += referencePhase
        val steps = mutableListOf("Опорный элемент: Φ₀ = ${phaseLabel(referencePhase)}")
        symbols.forEachIndexed { i, keep ->
            val prev = phases.last()
            val next = if (keep) prev else 1 - prev
            phases += next
            val symbol = if (keep) alphabet.one else alphabet.zero
            val rule = if (keep) "фаза сохраняется" else "фаза меняется на π"
            steps += "λ${sub(i + 1)} = $symbol: $rule ⇒ Φ${sub(i + 1)} = ${phaseLabel(next)}"
        }
        Encoded(phases, steps)
    }

    fun decode(phases: List<Int>): Result<Decoded> = runCatching {
        require(phases.size >= 2) { "Нужно минимум две фазы (первая — опорная)" }
        require(phases.all { it == 0 || it == 1 }) { "Фазы должны быть 0 или π" }
        val steps = mutableListOf("Первый элемент — опорный, его символ теряется при приёме")
        val symbols = (1 until phases.size).map { k ->
            val same = phases[k] == phases[k - 1]
            steps += "Φ${sub(k)} − Φ${sub(k - 1)} = ${if (same) "0" else "π"} ⇒ λ${sub(k)} = ${if (same) "1" else "0 (−1)"}"
            same
        }
        Decoded(symbols, steps)
    }

    companion object {
        fun phaseLabel(phase: Int): String = if (phase == 0) "0" else "π"

        private fun sub(n: Int): String = n.toString().map { "₀₁₂₃₄₅₆₇₈₉"[it - '0'] }.joinToString("")
    }
}
