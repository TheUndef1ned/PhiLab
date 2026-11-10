package app.philab.common

import app.philab.common.data.datasource.PhiTableJsonParser
import app.philab.common.domain.block.BlockReceptionUseCase
import app.philab.common.domain.dpsk.DpskCodecUseCase
import app.philab.common.domain.math.Bessel
import app.philab.common.domain.math.MarcumQ
import app.philab.common.domain.math.NormalDistribution
import app.philab.common.domain.normal.NormalProbabilityUseCase
import app.philab.common.domain.phi.model.PhiMethod
import app.philab.common.domain.phi.usecase.InversePhiUseCase
import app.philab.common.domain.phi.usecase.LookupPhiUseCase
import app.philab.common.domain.reception.model.Coherence
import app.philab.common.domain.reception.model.Modulation
import app.philab.common.domain.reception.model.ReceptionParams
import app.philab.common.domain.reception.model.SweepRequest
import app.philab.common.domain.reception.model.SweepVariable
import app.philab.common.domain.reception.model.ThresholdMode
import app.philab.common.domain.reception.usecase.BuildSweepUseCase
import app.philab.common.domain.reception.usecase.CalculateReceptionUseCase
import app.philab.common.domain.reception.usecase.RequiredSnrUseCase
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.domain.shared.parse.ExpressionParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/** Reference answers computed independently (Python math.erfc) for the practice tasks. */
class PracticeTasksTest {

    private val table by lazy {
        PhiTableJsonParser.parse(File("src/main/assets/phi_table.json").readText())
    }
    private val reception = CalculateReceptionUseCase()

    private fun assertRel(expected: Double, actual: Double, rel: Double = 1e-4) {
        assertTrue("expected $expected, got $actual", abs(actual - expected) <= rel * abs(expected))
    }

    // ---------------------------------------------------------------- table

    @Test
    fun `table matches analytic Phi in printed precision`() {
        assertEquals(500, table.size)
        for (i in 0 until table.size) {
            val exact = NormalDistribution.cdf(table.zAt(i))
            val halfUlp = 0.5 * Math.pow(10.0, -table.decimalsAt(i).toDouble())
            assertTrue("z=${table.zAt(i)}", abs(table.valueAt(i) - exact) <= halfUlp + 1e-15)
        }
    }

    @Test
    fun `example from the photo 3_57`() {
        val r = LookupPhiUseCase.lookup(table, 3.57)
        assertEquals(PhiMethod.TABLE, r.method)
        assertEquals(0.9998215, r.phi, 1e-12)
        assertEquals("0,9³8215", NumberFormatter.tableNotation(r.phi, r.cellDecimals!!))
    }

    @Test
    fun `book notation keeps single nine plain`() {
        assertEquals("0,98928", NumberFormatter.tableNotation(0.98928, 5))
        assertEquals("0,9²1802", NumberFormatter.tableNotation(0.991802, 6))
        assertEquals("0,8849", NumberFormatter.tableNotation(0.8849, 4))
    }

    @Test
    fun `negative z and interpolation`() {
        val neg = LookupPhiUseCase.lookup(table, -1.96)
        assertTrue(neg.usedSymmetry)
        assertEquals(0.025, neg.phi, 1e-9)
        val mid = LookupPhiUseCase.lookup(table, 1.965)
        assertEquals(PhiMethod.INTERPOLATED, mid.method)
        assertEquals(NormalDistribution.cdf(1.965), mid.phi, 2e-5)
        val far = LookupPhiUseCase.lookup(table, 6.0)
        assertEquals(PhiMethod.ANALYTIC, far.method)
    }

    @Test
    fun `inverse lookup`() {
        assertEquals(1.96, InversePhiUseCase.inverse(table, 0.975).z, 1e-3)
        assertEquals(-1.96, InversePhiUseCase.inverse(table, 0.025).z, 1e-3)
        assertEquals(3.0902, InversePhiUseCase.inverse(table, 1 - 1e-3).z, 2e-3)
    }

    @Test
    fun `osipov approximation within 0_2 percent`() {
        var z = 0.0
        while (z <= 4.99) {
            val rel = abs(NormalDistribution.osipovApproximation(z) - NormalDistribution.cdf(z)) / NormalDistribution.cdf(z)
            assertTrue("z=$z rel=$rel", rel < 2e-3)
            z += 0.01
        }
    }

    // ------------------------------------------------------------- practice 9

    @Test
    fun `practice 9 tasks 1 and 2`() {
        val uc = NormalProbabilityUseCase()
        assertRel(0.1855466847613488, uc(NormalProbabilityUseCase.Params(3.0, 5.0, 5.0)).greater)
        assertRel(0.03681913506015133, uc(NormalProbabilityUseCase.Params(3.0, 5.0, -1.0)).less)
    }

    // ------------------------------------------------------------- practice 3

    @Test
    fun `practice 3 task 1 neyman pearson vs ideal observer`() {
        val np = reception(
            ReceptionParams(modulation = Modulation.AT, q = 16.0, p0 = 0.1, threshold = ThresholdMode.NeymanPearson(1e-3))
        ).getOrThrow()
        assertRel(12.360929224671253, np.gamma)
        assertRel(1e-3, np.alpha)
        assertRel(0.16342526595251683, np.pe)

        val io = reception(ReceptionParams(modulation = Modulation.AT, q = 16.0, p0 = 0.1)).getOrThrow()
        assertRel(5.802775422663781, io.gamma)
        assertRel(0.012200443911165166, io.pe)
    }

    @Test
    fun `classic closed forms for equal priors`() {
        val q = 10.0
        assertRel(NormalDistribution.upperTail(sqrt(q / 4)), reception(ReceptionParams(modulation = Modulation.AT, q = q)).getOrThrow().pe)
        assertRel(NormalDistribution.upperTail(sqrt(q / 2)), reception(ReceptionParams(modulation = Modulation.FSK, q = q)).getOrThrow().pe)
        assertRel(NormalDistribution.upperTail(sqrt(q)), reception(ReceptionParams(modulation = Modulation.PSK, q = q)).getOrThrow().pe)
    }

    @Test
    fun `practice 4 task 2 code 4_3 PSK`() {
        val opt = reception(ReceptionParams(modulation = Modulation.PSK, q = 15.0, p0 = 3.0 / 7.0)).getOrThrow()
        assertRel(-0.2876820724517809, opt.gamma)
        assertRel(5.3171319394361283e-05, opt.pe)
        val zero = reception(
            ReceptionParams(modulation = Modulation.PSK, q = 15.0, p0 = 3.0 / 7.0, threshold = ThresholdMode.Fixed(0.0))
        ).getOrThrow()
        assertRel(5.3755588364741316e-05, zero.pe)
    }

    @Test
    fun `practice 1 task 3 s and minus 2s`() {
        val r = reception(
            ReceptionParams(modulation = Modulation.CUSTOM, q = 10.0, customK1 = 1.0, customK0 = 4.0, customRho = -1.0)
        ).getOrThrow()
        assertRel(1.0507179780062227e-06, r.pe, 1e-3)
        assertRel(-3.521825181113625, r.lossVsPskDb!!)
    }

    @Test
    fun `practice 1 task 2 PSK 120 degrees loss`() {
        val r = reception(ReceptionParams(modulation = Modulation.PSK, q = 10.0, pskDeviationDeg = 120.0)).getOrThrow()
        assertRel(10 * kotlin.math.log10(2.0 / 1.5), r.lossVsPskDb!!)
    }

    // ------------------------------------------------------------- practice 6

    @Test
    fun `practice 6 required snr`() {
        val uc = RequiredSnrUseCase(reception)
        val r1 = uc(1e-4).getOrThrow()
        assertRel(55.3243344763645, r1.rows.first { it.title == "АТ, когерентный" }.q, 1e-6)
        val r2 = uc(1e-5).getOrThrow()
        assertRel(36.378586968175306, r2.rows.first { it.title == "ЧТ, когерентный" }.q, 1e-6)
        assertRel(43.27911313764113, r2.rows.first { it.title == "ЧТ, некогерентный" }.q, 1e-9)
        assertRel(0.754325267471829, r2.comparisons.first { it.title.startsWith("ЧТ: некогерентный") }.deltaDb, 1e-5)
        // exact noncoherent AT needs a bit more SNR than coherent
        val atExact = r1.rows.first { it.title == "АТ, некогерентный (точно)" }.q
        assertTrue(atExact > 55.32)
    }

    @Test
    fun `practice 6 task 3 and 7 task 1 dpsk`() {
        val codec = DpskCodecUseCase()
        val symbols = listOf(1, -1, -1, -1, 1, 1, -1, 1, -1, 1).map { it == 1 }
        val enc = codec.encode(symbols, 0, DpskCodecUseCase.Alphabet.BIPOLAR).getOrThrow()
        assertEquals(listOf(0, 0, 1, 0, 1, 1, 1, 0, 0, 1, 1), enc.phases)

        val dec = codec.decode(listOf(0, 0, 1, 1, 1, 0, 1, 0, 0, 1, 1)).getOrThrow()
        assertEquals(listOf(true, false, true, true, false, false, false, true, false, true), dec.symbols)
    }

    // --------------------------------------------------------- practice 7–8

    @Test
    fun `practice 7 task 3 sync word`() {
        val p = reception(ReceptionParams(modulation = Modulation.PSK, q = 4.0)).getOrThrow().pe
        assertRel(0.022750131948179216, p)
        val r = BlockReceptionUseCase()(BlockReceptionUseCase.Params(15, 2, p)).getOrThrow()
        assertRel(0.9956362830648424, r.pCorrect)
    }

    @Test
    fun `practice 8 task 2 FSK noncoherent command`() {
        val p = reception(
            ReceptionParams(coherence = Coherence.NONCOHERENT, modulation = Modulation.FSK, q = 6.0)
        ).getOrThrow().pe
        assertRel(0.11156508007421491, p)
        val r = BlockReceptionUseCase()(BlockReceptionUseCase.Params(7, 0, p, 0.99)).getOrThrow()
        assertRel(0.43689728014433066, r.pCorrect)
        assertRel(0.0078125, r.falseTriggerHalf)
        assertTrue(r.pAfterRepetitions!! >= 0.99)
    }

    // ------------------------------------------------------------ noncoherent

    @Test
    fun `marcum q limits`() {
        assertEquals(exp(-0.5 * 4.0), MarcumQ.q(0.0, 2.0), 1e-6)
        assertEquals(1.0, MarcumQ.q(3.0, 0.0), 0.0)
        assertEquals(1.0, MarcumQ.q(3.0, 1.0) + MarcumQ.cdf(3.0, 1.0), 1e-6)
    }

    @Test
    fun `bessel arg ln i0 is inverse`() {
        for (x in listOf(0.3, 1.0, 5.0, 20.0, 80.0)) {
            assertEquals(x, Bessel.argLnI0(Bessel.lnI0(x)), 1e-6)
        }
        // I0(1) = 1.2660658777520082
        assertEquals(ln(1.2660658777520082), Bessel.lnI0(1.0), 1e-10)
    }

    @Test
    fun `noncoherent AT tends to lecture approximation`() {
        val r = reception(ReceptionParams(coherence = Coherence.NONCOHERENT, modulation = Modulation.AT, q = 100.0)).getOrThrow()
        val approx = 0.5 * exp(-100.0 / 8)
        assertTrue("pe=${r.pe} approx=$approx", r.pe / approx in 0.2..1.5)
        // optimal threshold ≈ half of amplitude at large SNR
        assertEquals(sqrt(100.0) / 2, r.gammaOptimal, 0.6)
    }

    @Test
    fun `noncoherent PSK is rejected`() {
        assertTrue(reception(ReceptionParams(coherence = Coherence.NONCOHERENT, modulation = Modulation.PSK)).isFailure)
    }

    @Test
    fun `sweep over p0 includes optimal comparison`() {
        val sweep = BuildSweepUseCase(reception)(
            SweepRequest(
                base = ReceptionParams(modulation = Modulation.FSK, q = 5.0, threshold = ThresholdMode.Fixed(0.0)),
                variable = SweepVariable.P0,
                from = 0.0,
                to = 1.0,
                points = 11,
            )
        ).getOrThrow()
        assertEquals(2, sweep.series.size)
        // optimal threshold is never worse
        sweep.series[0].points.zip(sweep.series[1].points).forEach { (fixed, opt) ->
            assertTrue(opt.y <= fixed.y + 1e-12)
        }
    }

    // ------------------------------------------------------------------ input

    @Test
    fun `expression parser`() {
        assertEquals(0.25, ExpressionParser.parse("1/4")!!, 0.0)
        assertEquals(1e-3, ExpressionParser.parse("10^-3")!!, 1e-18)
        assertEquals(1e-3, ExpressionParser.parse("1e-3")!!, 0.0)
        assertEquals(2.24, ExpressionParser.parse("2,24")!!, 0.0)
        assertEquals(-1.0, ExpressionParser.parse("−1")!!, 0.0)
        assertEquals(2 / sqrt(5.0), ExpressionParser.parse("2/√5")!!, 1e-15)
        assertEquals(ln(3.0 / 4.0), ExpressionParser.parse("ln(3/4)")!!, 1e-15)
        assertEquals(Math.PI / 3, ExpressionParser.parse("π/3")!!, 1e-15)
        assertEquals(null, ExpressionParser.parse("abc"))
    }
}
