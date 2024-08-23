package com.inky.fitnesscalendar.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.tooling.preview.Preview
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.TrackSvg
import com.inky.fitnesscalendar.util.gpx.simplify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun TrackView(track: TrackSvg, color: Color, modifier: Modifier, alpha: Float = 0.25f) {
    var trackPath by remember { mutableStateOf<Path?>(null) }
    LaunchedEffect(track) {
        launch(Dispatchers.Default) {
            trackPath = generateTrackPath(track)
        }
    }

    Canvas(modifier = modifier) {
        val path = trackPath
        if (path != null) {
            val trackWidth = track.bounds.longitudeMax - track.bounds.longitudeMin
            val trackHeight = track.bounds.latitudeMax - track.bounds.latitudeMin

            val strokeWidth = 8f

            val width = size.width
            val height = size.height

            val widthScale = width / trackWidth
            val heightScale = height / trackHeight
            val scale = minOf(widthScale, heightScale)

            val maxX = trackWidth * scale
            val maxY = trackHeight * scale

            // align right
            val offX = (-track.bounds.longitudeMin + (width - maxX)) / scale
            // center vertically
            val offY = (track.bounds.latitudeMax - (size.height - maxY) / 2f) / scale

            scale(
                scaleX = scale.toFloat(),
                scaleY = -scale.toFloat(),
                pivot = Offset(
                    track.bounds.longitudeMin.toFloat(),
                    track.bounds.latitudeMax.toFloat()
                )
            ) {
                translate(left = offX.toFloat(), top = offY.toFloat()) {
                    drawPath(
                        path,
                        color = color,
                        style = Stroke(width = (strokeWidth / scale).toFloat()),
                        alpha = alpha
                    )
                }
            }
        }
    }
}

private fun generateTrackPath(track: TrackSvg): Path {
    val path = Path()

    for ((index, point) in track.points.withIndex()) {
        val x = point.longitude.toFloat()
        val y = point.latitude.toFloat()
        if (index == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    return path
}

@Preview
@Composable
fun PreviewTrackView() {
    val trackPoints = remember {
        listOf(
            Coordinate(0.0, 0.0),
            Coordinate(1.0, 1.0),
            Coordinate(-0.5, 2.0),
            Coordinate(-1.0, 3.0),
            Coordinate(-1.0, 4.0),
            Coordinate(0.0, 5.0),
            Coordinate(2.0, 4.5),
            Coordinate(3.0, 2.0),
            Coordinate(0.0, 0.0),
        )
    }
    val simplifiedTrack = remember(trackPoints) { simplify(trackPoints, maxNumPoints = 7) }
    val path = remember(simplifiedTrack) { TrackSvg.fromPoints(simplifiedTrack) }

    if (path != null) {
        Surface(modifier = Modifier.fillMaxSize()) {
            TrackView(track = path, color = Color.Red, modifier = Modifier.fillMaxSize())
        }
    }
}