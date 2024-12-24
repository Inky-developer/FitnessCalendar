package com.inky.fitnesscalendar.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class PieChartState<T>(val dataPoints: List<PieChartEntry<T>>) {
    fun sum(): Double = dataPoints.sumOf { it.value }

    fun segmentByAngle(angleDegrees: Float): T {
        assert(angleDegrees in 0.0..360.0)

        val total = sum()
        var lastAngle = 0.0
        for (segment in dataPoints) {
            val segmentAngle = segment.value / total * 360
            val angleEnd = lastAngle + segmentAngle
            if (angleDegrees in lastAngle..<angleEnd) {
                return segment.payload
            }

            lastAngle = angleEnd
        }

        throw IllegalStateException("Angles don't add up to 360 or input is invalid")
    }
}

data class PieChartEntry<T>(val value: Double, val label: String, val color: Color, val payload: T)

@Composable
fun <T> PieChart(
    state: PieChartState<T>,
    fontSize: TextUnit = TextUnit.Unspecified,
    style: TextStyle = LocalTextStyle.current,
    onClick: (T) -> Unit = {},
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val angleAnimation = remember(state) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(state) {
        angleAnimation.animateTo(1.0f, animationSpec = tween(durationMillis = 1000))
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = style.merge(fontSize = fontSize)

    var canvasSize by remember { mutableStateOf(Size.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .pointerInput(onClick, state) {
                detectTapGestures { offset ->
                    val offsetFromCenter =
                        offset - Offset(canvasSize.width / 2, canvasSize.height / 2)
                    val angle = radToDeg(
                        -atan2(
                            offsetFromCenter.x,
                            offsetFromCenter.y
                        ) + 0.5f * PI.toFloat()
                    )
                    val segmentIndex = state.segmentByAngle(angle)
                    onClick(segmentIndex)
                }
            }
            .shadow(4.dp, shape = CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (canvasSize != size) {
            canvasSize = size
        }

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

            val textX: Float
            val textY: Float
            if (state.dataPoints.size == 1) {
                textX = 0f
                textY = 0f
            } else {
                textX = cos(centerAngleRad) * size.width / 4
                textY = sin(centerAngleRad) * size.height / 4
            }

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

private fun radToDeg(angleRadians: Float): Float =
    (angleRadians / (PI.toFloat() * 2) * 360).mod(360f)

@Preview(device = "spec:width=512px,height=1024px,dpi=440")
@Composable
private fun PreviewPieChart() {
    val state = PieChartState(
        listOf(
            PieChartEntry(20.0, "Entry a", Color.Red, Unit),
            PieChartEntry(10.0, "Entry b", Color.Black, Unit),
            PieChartEntry(5.0, "Entry c", Color.Green, Unit),
            PieChartEntry(3.0, "Entry d", Color.Yellow, Unit),
        ).sortedBy { it.value }
    )
    Surface(modifier = Modifier.fillMaxWidth()) {
        PieChart(state, animate = false)
    }
}

@Preview(device = "spec:width=512px,height=1024px,dpi=440")
@Composable
private fun PreviewPieChartSingleEntry() {
    val state = PieChartState(listOf(PieChartEntry(20.0, "Entry a", Color.Red, Unit)))
    Surface(modifier = Modifier.fillMaxWidth()) {
        PieChart(state, animate = false)
    }
}