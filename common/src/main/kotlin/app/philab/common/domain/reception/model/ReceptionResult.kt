package app.philab.common.domain.reception.model

data class ReceptionResult(
    val params: ReceptionParams,
    /** Probability of error of the first kind (false alarm, λ₀ → λ̂₁). */
    val alpha: Double,
    /** Probability of error of the second kind (miss, λ₁ → λ̂₀). */
    val beta: Double,
    /** Total error probability Pe = p₀α + p₁β. */
    val pe: Double,
    /** Threshold actually used. */
    val gamma: Double,
    /** Optimal (ideal observer) threshold for the same p₀, p₁. */
    val gammaOptimal: Double,
    /** Pe with the optimal threshold — for "сравнить с оптимальным порогом". */
    val peOptimal: Double,
    /** Coherent statistic parameters (null for noncoherent reception). */
    val m0: Double? = null,
    val m1: Double? = null,
    val sigma: Double? = null,
    /** Energy loss vs antipodal PSK at equal priors, dB (coherent only). */
    val lossVsPskDb: Double? = null,
    /** Large-SNR approximation from the lecture, if any. */
    val approximatePe: Double? = null,
    val steps: List<String> = emptyList(),
    val notes: List<String> = emptyList(),
)
