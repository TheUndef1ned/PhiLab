package app.philab.common.presentation.components.organisms.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.philab.common.domain.shared.format.NumberFormatter
import app.philab.common.presentation.theme.LabDimens
import app.philab.common.presentation.theme.LabFontFamily
import app.philab.common.presentation.theme.LabTheme
import app.philab.common.presentation.theme.LabSpacing
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

data class ChartPoint(val x: Double, val y: Double)

data class ChartSeries(val name: String, val points: List<ChartPoint>, val color: Color, val dashed: Boolean = false)

/**
 * Lightweight Canvas line chart (no third-party chart libs), log-Y for error probabilities.
 * Tap or drag to read the values at a given x.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LineChart(
    series: List<ChartSeries>,
    xLabel: String,
    yLabel: String,
    logY: Boolean,
    modifier: Modifier = Modifier,
) {
    val measurer = rememberTextMeasurer()
    val axisColor = LabTheme.colors.textMuted
    val gridColor = LabTheme.colors.divider
    val cursorColor = LabTheme.colors.primary
    val labelStyle = TextStyle(fontFamily = LabFontFamily, fontSize = 10.sp, color = axisColor)
    var cursorX by remember(series) { mutableStateOf<Float?>(null) }

    val all = series.flatMap { it.points }.filter { it.x.isFinite() && it.y.isFinite() && (!logY || it.y > 0) }
    if (all.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(LabDimens.chartHeight),
            contentAlignment = Alignment.Center
        ) {
            Text("Нет данных для графика", style = LabTheme.type.bodyMedium, color = axisColor)
        }
        return
    }

    val xMin = all.minOf { it.x }
    val xMaxRaw = all.maxOf { it.x }
    val xMax = if (xMaxRaw > xMin) xMaxRaw else xMin + 1.0
    val (yMin, yMax) = if (logY) {
        val ys = all.map { log10(it.y) }
        val top = ceil(ys.max()).coerceAtLeast(floor(ys.min()) + 1)
        val bottom = max(floor(ys.min()), top - 14)
        bottom to top
    } else {
        val lo = min(0.0, all.minOf { it.y })
        val hi = all.maxOf { it.y }.let { if (it <= lo) lo + 1 else it * 1.05 }
        lo to hi
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(LabDimens.chartHeight)
                .pointerInput(series) {
                    detectTapGestures { cursorX = it.x }
                }
                .pointerInput(series) {
                    detectDragGestures { change, _ -> cursorX = change.position.x }
                }
        ) {
            val left = 48.dp.toPx()
            val bottom = 30.dp.toPx()
            val top = 10.dp.toPx()
            val right = 10.dp.toPx()
            val w = size.width - left - right
            val h = size.height - top - bottom

            fun px(x: Double) = left + ((x - xMin) / (xMax - xMin) * w).toFloat()
            fun py(y: Double): Float {
                val v = if (logY) log10(y).coerceIn(yMin, yMax) else y
                return top + ((yMax - v) / (yMax - yMin) * h).toFloat()
            }

            // grid + y labels
            val yTicks: List<Pair<Double, String>> = if (logY) {
                val decades = (yMax - yMin).toInt()
                val stepDec = max(1, ceil(decades / 7.0).toInt())
                (yMin.toInt()..yMax.toInt() step stepDec).map { d ->
                    10.0.pow(d) to if (d == 0) "1" else "10${NumberFormatter.superscript(d)}"
                }
            } else {
                niceTicks(yMin, yMax, 5).map { it to NumberFormatter.format(it, 3) }
            }
            yTicks.forEach { (value, text) ->
                val y = py(value)
                drawLine(gridColor, Offset(left, y), Offset(left + w, y), strokeWidth = 1f)
                val layout = measurer.measure(text, labelStyle)
                drawText(layout, topLeft = Offset(left - layout.size.width - 6f, y - layout.size.height / 2f))
            }
            niceTicks(xMin, xMax, 5).forEach { value ->
                val x = px(value)
                drawLine(gridColor, Offset(x, top), Offset(x, top + h), strokeWidth = 1f)
                val layout = measurer.measure(NumberFormatter.format(value, 4), labelStyle)
                drawText(layout, topLeft = Offset(x - layout.size.width / 2f, top + h + 6f))
            }
            drawLine(axisColor, Offset(left, top), Offset(left, top + h), strokeWidth = 2f)
            drawLine(axisColor, Offset(left, top + h), Offset(left + w, top + h), strokeWidth = 2f)

            // series
            series.forEach { s ->
                val path = Path()
                var started = false
                s.points.forEach { p ->
                    if (!p.y.isFinite() || (logY && p.y <= 0)) {
                        started = false
                        return@forEach
                    }
                    val o = Offset(px(p.x), py(p.y))
                    if (!started) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
                    started = true
                }
                drawPath(
                    path = path,
                    color = s.color,
                    style = Stroke(
                        width = 2.5.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round,
                        pathEffect = if (s.dashed) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                    )
                )
            }

            // cursor
            cursorX?.let { cx ->
                val clamped = cx.coerceIn(left, left + w)
                val xValue = xMin + (clamped - left) / w * (xMax - xMin)
                drawLine(
                    cursorColor,
                    Offset(clamped, top),
                    Offset(clamped, top + h),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                )
                var labelY = top + 4f
                val xl = measurer.measure("x = ${NumberFormatter.format(xValue, 4)}", labelStyle.copy(color = cursorColor))
                val labelX = if (clamped + xl.size.width + 8 > left + w) clamped - xl.size.width - 6 else clamped + 6
                drawText(xl, topLeft = Offset(labelX, labelY))
                labelY += xl.size.height
                series.forEach { s ->
                    val nearest = s.points.minByOrNull { abs(it.x - xValue) } ?: return@forEach
                    if (!nearest.y.isFinite() || (logY && nearest.y <= 0)) return@forEach
                    drawCircle(s.color, radius = 4.dp.toPx(), center = Offset(px(nearest.x), py(nearest.y)))
                    val t = measurer.measure(NumberFormatter.probability(nearest.y), labelStyle.copy(color = s.color))
                    drawText(t, topLeft = Offset(labelX, labelY))
                    labelY += t.size.height
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = LabSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(yLabel + if (logY) " (лог.)" else "", style = LabTheme.type.hint, color = axisColor)
            Text(xLabel, style = LabTheme.type.hint, color = axisColor)
        }
        FlowRow(
            modifier = Modifier.padding(top = LabSpacing.s),
            horizontalArrangement = Arrangement.spacedBy(LabSpacing.m),
            verticalArrangement = Arrangement.spacedBy(LabSpacing.xs)
        ) {
            series.forEach { s ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(s.color, CircleShape)
                    )
                    Text(
                        text = s.name,
                        style = LabTheme.type.bodySmall,
                        color = LabTheme.colors.textSecondary,
                        modifier = Modifier.padding(start = LabSpacing.xs)
                    )
                }
            }
        }
    }
}

private fun niceTicks(min: Double, max: Double, count: Int): List<Double> {
    val range = max - min
    if (range <= 0) return listOf(min)
    val rough = range / count
    val mag = 10.0.pow(floor(log10(rough)))
    val norm = rough / mag
    val step = when {
        norm < 1.5 -> 1.0
        norm < 3.0 -> 2.0
        norm < 7.0 -> 5.0
        else -> 10.0
    } * mag
    val start = ceil(min / step) * step
    val ticks = mutableListOf<Double>()
    var v = start
    while (v <= max + step * 1e-9) {
        ticks += if (abs(v) < step * 1e-9) 0.0 else v
        v += step
    }
    return ticks
}

/** Palette for series (accent first, then semantic colors). */
@Composable
fun chartColors(): List<Color> = listOf(
    LabTheme.colors.primary,
    LabTheme.colors.success,
    LabTheme.colors.warning,
    LabTheme.colors.error,
    LabTheme.colors.textSecondary,
)
