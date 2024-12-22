package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class PieChartState(val dataPoints: List<PieChartEntry>) {
    fun sum(): Double = dataPoints.sumOf { it.value }
}

data class PieChartEntry(val value: Double, val label: String, val color: Color)

@Composable
fun PieChart(
    state: PieChartState,
    fontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    animate: Boolean = true
) {
    val angleAnimation = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(Unit) {
        angleAnimation.animateTo(1.0f, animationSpec = tween(durationMillis = 1000))
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = style.merge(fontSize = fontSize)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .shadow(4.dp, shape = CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        var angleStart = 0f
        val sum = state.sum()

        for (dataPoint in state.dataPoints) {
            val percentage = dataPoint.value.toFloat() / sum.toFloat() * 100 * angleAnimation.value
            val angle = percentage * 3.6f

            drawArc(dataPoint.color, angleStart, angle, true)

            angleStart += angle
        }

        angleStart = 0f
        for (dataPoint in state.dataPoints) {
            val angle = dataPoint.value.toFloat() / sum.toFloat() * 360 * angleAnimation.value
            val centerAngleRad = degToRad(angleStart + angle / 2)
            val textX = cos(centerAngleRad) * size.width / 4
            val textY = sin(centerAngleRad) * size.height / 4

            // This is how available space is calculated:
            // First, assume the slice is not rotated. This means that the available width is
            // the radius. For available height calculate the height (y-coordinate of endpoint).
            // Then apply the actual rotation to these values. For example, if the rotation is 90 degrees,
            // available width and available height will just swap, since the slice is now pointing down.
            val curAngleRad = degToRad(angle)
            val endPointY = sin(curAngleRad) * size.height / 2
            val initialAvailableHeight = if (angle > 90) size.height / 2 else abs(endPointY / 2f)
            val initialAvailableWidth = size.width / 2
            val availableWidth =
                interpolateCircle(centerAngleRad, initialAvailableWidth, initialAvailableHeight)
            val availableHeight =
                interpolateCircle(centerAngleRad, initialAvailableHeight, initialAvailableWidth)

            val measuredText = textMeasurer.measure(
                dataPoint.label,
                maxLines = 2,
                overflow = TextOverflow.Visible,
                style = textStyle,
                constraints = Constraints.fitPrioritizingWidth(
                    0,
                    availableWidth.roundToInt(),
                    0,
                    availableHeight.roundToInt()
                )
            )
            if (!measuredText.didOverflowWidth && !measuredText.didOverflowHeight) {
                translate(
                    left = (size.width - measuredText.size.width) / 2 + textX,
                    top = (size.height - measuredText.size.height) / 2f + textY
                ) {
                    drawRect(
                        Color.White,
                        alpha = 0.4f,
                        size = measuredText.size.toSize()
                    )
                    drawText(measuredText)
                }
            }

            angleStart += angle
        }
    }
}

/**
 * Interpolates between the two given values periodically every 90 degrees (0.5PI rad)
 */
private fun interpolateCircle(angleRad: Float, max: Float, min: Float): Float {
    val base = 0.5f * (max - min)
    return base * cos(angleRad) + base + min
}

private fun degToRad(angleDegrees: Float): Float = angleDegrees / 360 * PI.toFloat() * 2

@Preview(device = "spec:width=512px,height=1024px,dpi=440")
@Composable
private fun PreviewPieChart() {
    val state = PieChartState(
        listOf(
            PieChartEntry(20.0, "Entry a", Color.Red),
            PieChartEntry(10.0, "Entry b", Color.Black),
            PieChartEntry(5.0, "Entry c", Color.Green),
            PieChartEntry(3.0, "Entry d", Color.Yellow),
        ).sortedBy { it.value }
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        PieChart(state, animate = false)
    }
}