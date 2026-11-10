package app.philab.common.domain.block

import app.philab.common.domain.math.Combinatorics
import app.philab.common.domain.shared.format.NumberFormatter
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.ln

/**
 * Element-by-element reception of a word of N symbols (command, sync word) — practice 7–8.
 * The word is accepted if no more than k symbols are wrong:
 *   P = Σ_{i=0..k} C(N, i)·p^i·(1 − p)^{N−i}.
 */
class BlockReceptionUseCase @Inject constructor() {

    data class Params(
        val length: Int,
        val maxErrors: Int,
        val symbolErrorProbability: Double,
        /** Required probability for "сколько раз передать команду". */
        val targetProbability: Double? = null,
    )

    data class Term(val errors: Int, val probability: Double)

    data class Result(
        val pCorrect: Double,
        val pFail: Double,
        val terms: List<Term>,
        val repetitions: Int?,
        val pAfterRepetitions: Double?,
        /** Noise-only trigger when every element is guessed with probability ½ (FSK, comparison of envelopes). */
        val falseTriggerHalf: Double,
        val steps: List<String>,
    )

    operator fun invoke(params: Params): kotlin.Result<Result> = runCatching {
        val n = params.length
        val k = params.maxErrors
        val p = params.symbolErrorProbability
        require(n in 1..10_000) { "N должно быть от 1 до 10000" }
        require(k in 0..n) { "Допустимое число ошибок от 0 до N" }
        require(p in 0.0..1.0) { "p должно быть в [0; 1]" }

        val terms = (0..k).map { Term(it, Combinatorics.binomialPmf(n, it, p)) }
        val pCorrect = terms.sumOf { it.probability }.coerceIn(0.0, 1.0)
        val pFail = 1.0 - pCorrect

        var repetitions: Int? = null
        var pAfter: Double? = null
        params.targetProbability?.let { target ->
            require(target > 0.0 && target < 1.0) { "Требуемая вероятность в (0; 1)" }
            repetitions = when {
                pCorrect >= target -> 1
                pCorrect <= 0.0 -> null
                else -> ceil(ln(1.0 - target) / ln(1.0 - pCorrect)).toInt()
            }
            repetitions?.let { r -> pAfter = 1.0 - Math.pow(1.0 - pCorrect, r.toDouble()) }
        }

        val falseTrigger = Combinatorics.binomialCdf(n, k, 0.5)

        val steps = buildList {
            add("p = ${pr(p)}, 1 − p = ${pr(1 - p)}, N = $n, допускается ошибок: $k")
            terms.forEach { t ->
                add("i = ${t.errors}: C($n, ${t.errors})·p^${t.errors}·(1−p)^${n - t.errors} = ${pr(t.probability)}")
            }
            add("P = Σ = ${pr(pCorrect)}")
            repetitions?.let { r ->
                add("1 − (1 − P)^n ≥ ${pr(params.targetProbability!!)} ⇒ n ≥ ln(1 − P_треб)/ln(1 − P) ⇒ n = $r")
                add("При n = $r: P = ${pr(pAfter!!)}")
            }
            add("Срабатывание по шумам (каждый элемент — ½): ${pr(falseTrigger)}")
        }

        Result(
            pCorrect = pCorrect,
            pFail = pFail,
            terms = terms,
            repetitions = repetitions,
            pAfterRepetitions = pAfter,
            falseTriggerHalf = falseTrigger,
            steps = steps,
        )
    }

    private fun pr(v: Double) = NumberFormatter.probability(v)
}
