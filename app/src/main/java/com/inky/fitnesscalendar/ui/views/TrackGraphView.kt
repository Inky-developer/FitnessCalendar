package com.inky.fitnesscalendar.ui.views

import android.graphics.Typeface
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.Instant
import java.util.Date
import kotlin.math.roundToLong


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackGraphView(
    viewModel: BaseViewModel = hiltViewModel(),
    activityId: Int,
    projection: TrackGraphProjection,
    onBack: () -> Unit
) {
    val track by remember(activityId) { viewModel.repository.getTrackByActivity(activityId) }
        .collectAsState(null)

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.Chart)) },
                colors = defaultTopAppBarColors(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            AnimatedContent(track, label = "TrackGraphOrLoadingIndicator") { actualTrack ->
                when (actualTrack) {
                    null -> CircularProgressIndicator()
                    else -> TrackGraph(actualTrack, projection, modifier = Modifier.fillMaxSize())
                }
            }
        }
    }
}

@Composable
fun TrackGraph(track: Track, projection: TrackGraphProjection, modifier: Modifier = Modifier) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(track) {
        withContext(Dispatchers.Default) {
            val values = projection.apply(track)
            modelProducer.runTransaction {
                lineSeries {
                    series(values.keys, values.values)
                }
            }
        }
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(colorResource(projection.color))),
                        thickness = 1.dp
                    )
                )
            ),
            startAxis = VerticalAxis.rememberStart(
                title = stringResource(projection.verticalAxisLabel),
                titleComponent = legendComponent()
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                title = stringResource(R.string.legend_distance_km),
                titleComponent = legendComponent(),
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(
                    addExtremeLabelPadding = true,
                    spacing = Distance(kilometers = 1.0).meters.toInt()
                ),
                valueFormatter = rememberKmValueFormatter()
            ),
            marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                guideline = rememberAxisGuidelineComponent(),
                valueFormatter = rememberMarkerFormatter(projection)
            )
        ),
        modelProducer = modelProducer,
        zoomState = rememberVicoZoomState(initialZoom = Zoom.Content),
        modifier = modifier
    )
}

@Composable
private fun legendComponent() = rememberTextComponent(
    background = rememberShapeComponent(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CorneredShape.Pill,
        margins = Dimensions(allDp = 2f)
    ),
    color = MaterialTheme.colorScheme.onSecondaryContainer,
    padding = dimensions(horizontal = 8.dp, vertical = 4.dp),
    typeface = Typeface.MONOSPACE
)

@Composable
private fun rememberMarkerFormatter(projection: TrackGraphProjection): DefaultCartesianMarkerValueFormatter {
    val unit = stringResource(projection.unit)

    return remember(projection) {
        DefaultCartesianMarkerValueFormatter(decimalFormat = DecimalFormat("#.#$unit;-#.#$unit"))
    }
}

@Composable
private fun rememberKmValueFormatter(): CartesianValueFormatter {
    val unit = stringResource(R.string.unit_km)
    val format = DecimalFormat("#.#$unit;-#.#$unit")
    return CartesianValueFormatter { _, distanceMeters, _ ->
        val kilometers = Distance(meters = distanceMeters.roundToLong()).kilometers
        format.format(kilometers)
    }
}

enum class TrackGraphProjection(
    @ColorRes val color: Int,
    @StringRes val verticalAxisLabel: Int,
    @StringRes val unit: Int
) {
    Speed(R.color.graph_speed, R.string.legend_speed_kmh, R.string.unit_kmh),
    HeartRate(R.color.graph_heart_rate, R.string.legend_heart_rate_bpm, R.string.unit_bpm),
    Elevation(R.color.graph_elevation, R.string.legend_elevation_m, R.string.unit_m),
    Temperature(
        R.color.graph_temperature,
        R.string.legend_temperature_celsius,
        R.string.unit_celsius
    );

    fun apply(track: Track): Map<Long, Float> {
        val points = track.computedPoints()
        val result = mutableMapOf<Long, Float>()

        for (point in points) {
            mapPoint(point)?.let { result[point.cumDistance.meters] = it }
        }

        return result
    }

    private fun mapPoint(point: Track.ComputedTrackPoint): Float? = when (this) {
        Speed -> point.speed.kmh.toFloat()
        HeartRate -> point.point.heartFrequency?.bpm
        Elevation -> point.point.elevation?.meters
        Temperature -> point.point.temperature?.celsius
    }
}

@Preview
@Composable
fun PreviewTrackGraph() {
    val trackPointTime = Date.from(Instant.now())
    val track = Track(
        activityId = -1, points = listOf(
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 0f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 10f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 5f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 10f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 20f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 17f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 6f)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 0f)
            ),
        )
    )

    TrackGraph(track, TrackGraphProjection.Elevation, modifier = Modifier.fillMaxSize())
}