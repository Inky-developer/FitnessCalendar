package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.graphics.RectF
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
import androidx.compose.ui.platform.LocalContext
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
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerDrawingModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DecimalFormat
import java.time.Instant
import java.util.Date
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt
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
            rememberLODLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(
                    LineCartesianLayer.rememberLine(
                        fill = LineCartesianLayer.LineFill.single(fill(colorResource(projection.color))),
                        thickness = 1.dp
                    )
                ),
                rangeProvider = projection.rangeProvider()
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
    val context = LocalContext.current
    return remember(projection) {
        DefaultCartesianMarkerValueFormatter(decimalFormat = projection.format(context))
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

// This annotation is needed as long as [TrackGraphProjection] is used as a NavArg
// See https://issuetracker.google.com/issues/358687142
@androidx.annotation.Keep
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

    fun format(context: Context): DecimalFormat {
        val unitString = context.getString(unit)
        return when (this) {
            HeartRate, Elevation -> DecimalFormat("0$unitString")
            else -> DecimalFormat("#.#$unitString")
        }
    }

    fun rangeProvider() = when (this) {
        Elevation -> ElevationLayerRangeProvider()
        else -> CartesianLayerRangeProvider.auto()
    }

    private fun mapPoint(point: Track.ComputedTrackPoint): Float? = when (this) {
        Speed -> point.speed.kmh.toFloat()
        HeartRate -> point.point.heartFrequency?.bpm
        Elevation -> point.point.elevation?.meters
        Temperature -> point.point.temperature?.celsius
    }
}

@Composable
private fun rememberLODLineCartesianLayer(
    lineProvider: LineCartesianLayer.LineProvider,
    rangeProvider: CartesianLayerRangeProvider
) =
    remember(lineProvider) {
        LODLineCartesianLayer(lineProvider = lineProvider, rangeProvider = rangeProvider)
    }

/**
 * A very hacky overwrite over [LineCartesianLayer], which supports Level of detail.
 * When zoomed out, it groups nearby points together and shows the average only,
 * but it reveals more details when zoomed in
 *
 * Most lines are copy-pasted from [LineCartesianLayer], with some changes to support the LOD
 * functionality.
 */
private class LODLineCartesianLayer(
    lineProvider: LineProvider,
    rangeProvider: CartesianLayerRangeProvider
) : LineCartesianLayer(
    lineProvider,
    rangeProvider = rangeProvider
) {
    private fun RectF.getStart(isLtr: Boolean): Float = if (isLtr) left else right

    private inline fun <T : CartesianLayerModel.Entry> List<T>.forEachIn(
        range: ClosedFloatingPointRange<Double>,
        padding: Int = 0,
        zoom: Float = 1f,
        // The minimal distance between two points to be separated
        // No idea what the unit is
        minDistance: Float = 0.05f,
        action: (List<T>, T?) -> Unit,
    ) {
        var start = 0
        var end = 0
        for (entry in this) {
            when {
                entry.x < range.start -> start++
                entry.x > range.endInclusive -> break
            }
            end++
        }
        start = (start - padding).coerceAtLeast(0)
        end = (end + padding).coerceAtMost(lastIndex)

        var lastIndex = start
        val pointsAccumulator = mutableListOf<T>()
        ((start + 1)..end).forEach {
            val point = this[it]
            val last = this[lastIndex]

            pointsAccumulator.add(point)

            val pointDistance = (point.x - last.x) * zoom
            if (pointDistance >= minDistance) {
                action(pointsAccumulator, getOrNull(it + 1))
                lastIndex = it
                pointsAccumulator.clear()
            }
        }
        if (pointsAccumulator.isNotEmpty()) {
            action(pointsAccumulator, null)
        }
    }

    override fun CartesianDrawingContext.forEachPointInBounds(
        series: List<LineCartesianLayerModel.Entry>,
        drawingStart: Float,
        pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.PointInfo>?,
        action: (entry: LineCartesianLayerModel.Entry, x: Float, y: Float, previousX: Float?, nextX: Float?) -> Unit
    ) {
        val minX = ranges.minX
        val maxX = ranges.maxX
        val xStep = ranges.xStep

        var x: Float? = null
        var nextX: Float? = null

        val boundsStart = layerBounds.getStart(isLtr = isLtr)
        val boundsEnd = boundsStart + layoutDirectionMultiplier * layerBounds.width()

        fun getDrawX(entry: LineCartesianLayerModel.Entry): Float =
            drawingStart +
                    layoutDirectionMultiplier *
                    horizontalDimensions.xSpacing *
                    ((entry.x - minX) / xStep).toFloat()

        fun getDrawY(entry: LineCartesianLayerModel.Entry): Float {
            val yRange = ranges.getYRange(verticalAxisPosition)
            return layerBounds.bottom -
                    (pointInfoMap?.get(entry.x)?.y
                        ?: ((entry.y - yRange.minY) / yRange.length).toFloat()) *
                    layerBounds.height()
        }

        fun pointAvg(points: List<LineCartesianLayerModel.Entry>): LineCartesianLayerModel.Entry {
            assert(points.isNotEmpty())

            return LineCartesianLayerModel.Entry(
                x = points.sumOf { it.x } / points.size,
                y = points.sumOf { it.y } / points.size
            )
        }

        // Round the zoom value to the next closest order of magnitude, to prevent too much flickering
        val effectiveZoom = 10.0.pow(log10(zoom).roundToInt()).toFloat()
        series.forEachIn(range = minX..maxX, padding = 1, zoom = effectiveZoom) { entries, next ->
            val entry = pointAvg(entries)
            val previousX = x
            val immutableX = nextX ?: getDrawX(entry)
            val immutableNextX = next?.let(::getDrawX)
            x = immutableX
            nextX = immutableNextX
            if (
                immutableNextX != null &&
                (isLtr && immutableX < boundsStart || !isLtr && immutableX > boundsStart) &&
                (isLtr && immutableNextX < boundsStart || !isLtr && immutableNextX > boundsStart)
            ) {
                return@forEachIn
            }
            action(entry, immutableX, getDrawY(entry), previousX, nextX)
            if (isLtr && immutableX > boundsEnd || isLtr.not() && immutableX < boundsEnd) return
        }
    }
}

// The resolution for the min y and max y values on the vertical graph axis
private const val ElevationGraphYStep = 100.0

/**
 * Alternative to the auto range provider, which usually starts at y-0.
 * That is not very good for elevation graphs, so instead, this class
 * provides a dynamic range based on [ElevationGraphYStep].
 */
private class ElevationLayerRangeProvider : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        floor(minY / ElevationGraphYStep) * ElevationGraphYStep

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        ceil(maxY / ElevationGraphYStep) * ElevationGraphYStep
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