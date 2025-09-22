package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.graphics.RectF
import android.graphics.Typeface
import android.text.SpannableStringBuilder
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColor
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.gpx.Coordinate
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Elevation
import com.inky.fitnesscalendar.data.measure.meters
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.defaultAreaFill
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberEnd
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.insets
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
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
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.Insets
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
    var overlayProjection by rememberSaveable { mutableStateOf<TrackGraphProjection?>(null) }

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
                actions = {
                    OverlaySelectButton(
                        baseProjection = projection,
                        overlay = overlayProjection,
                        onOverlay = { overlayProjection = it }
                    )
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
                    else -> TrackGraph(
                        actualTrack,
                        projection,
                        overlayProjection,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlaySelectButton(
    baseProjection: TrackGraphProjection,
    overlay: TrackGraphProjection?,
    onOverlay: (TrackGraphProjection?) -> Unit
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    TextButton(onClick = { menuOpen = true }) {
        Text(if (overlay == null) stringResource(R.string.no_overlay) else stringResource(overlay.shortName))
    }

    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.no_overlay)) },
            onClick = {
                menuOpen = false
                onOverlay(null)
            }
        )

        HorizontalDivider()

        for (projection in TrackGraphProjection.entries) {
            if (projection == baseProjection) continue

            DropdownMenuItem(
                text = { Text(stringResource(projection.shortName)) },
                onClick = {
                    menuOpen = false
                    onOverlay(projection)
                }
            )
        }
    }
}

@Composable
fun TrackGraph(
    track: Track,
    projection: TrackGraphProjection,
    overlay: TrackGraphProjection?,
    modifier: Modifier = Modifier
) {
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(track, overlay) {
        withContext(Dispatchers.Default) {
            val values = projection.apply(track)
            val overlayValues = overlay?.apply(track)

            modelProducer.runTransaction {
                if (overlayValues != null) {
                    lineSeries {
                        series(x = overlayValues.keys, y = overlayValues.values)
                    }
                }
                lineSeries {
                    series(values.keys, values.values)
                }
            }
        }
    }

    val context = LocalContext.current

    val layers = remember(projection, overlay) {
        val mainLayer = LODLineCartesianLayer(
            lineProvider = LineCartesianLayer.LineProvider.series(projection.line(context)),
            rangeProvider = projection.rangeProvider(),
            verticalAxisPosition = Axis.Position.Vertical.Start
        )
        val overlayLayer = overlay?.let {
            LODLineCartesianLayer(
                lineProvider = LineCartesianLayer.LineProvider.series(it.line(context)),
                rangeProvider = it.rangeProvider(),
                verticalAxisPosition = Axis.Position.Vertical.End
            )
        }
        listOfNotNull(overlayLayer, mainLayer).toTypedArray()
    }

    CartesianChartHost(
        chart = rememberCartesianChart(
            *layers,
            startAxis = VerticalAxis.rememberStart(
                title = stringResource(projection.verticalAxisLabel),
                titleComponent = legendComponent(),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
            ),
            endAxis = overlay?.let {
                VerticalAxis.rememberEnd(
                    title = stringResource(it.verticalAxisLabel),
                    titleComponent = legendComponent(),
                    horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                )
            },
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                title = stringResource(R.string.legend_distance_km),
                titleComponent = legendComponent(),
                itemPlacer = remember {
                    HorizontalAxis.ItemPlacer.aligned(addExtremeLabelPadding = true)
                },
                valueFormatter = rememberKmValueFormatter()
            ),
            marker = rememberDefaultCartesianMarker(
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                guideline = rememberAxisGuidelineComponent(),
                valueFormatter = rememberMarkerFormatter(projection, overlay)
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
        fill = fill(MaterialTheme.colorScheme.secondaryContainer),
        shape = CorneredShape.Pill,
        margins = Insets(allDp = 2f)
    ),
    color = MaterialTheme.colorScheme.onSecondaryContainer,
    padding = insets(horizontal = 8.dp, vertical = 4.dp),
    typeface = Typeface.MONOSPACE
)

@Composable
private fun rememberMarkerFormatter(
    projection: TrackGraphProjection,
    overlay: TrackGraphProjection?
): DefaultCartesianMarker.ValueFormatter {
    val context = LocalContext.current
    return remember(projection, overlay) {
        val formatters = listOfNotNull(
            overlay?.let {
                DefaultCartesianMarker.ValueFormatter.default(decimalFormat = it.format(context))
            },
            DefaultCartesianMarker.ValueFormatter.default(decimalFormat = projection.format(context)),
        )
        MultiMarkerFormatter(formatters)
    }
}

@Composable
private fun rememberKmValueFormatter(): CartesianValueFormatter {
    val unit = stringResource(R.string.unit_km)
    val format = DecimalFormat("#.#$unit;-#.#$unit")
    return CartesianValueFormatter { _, distanceMeters, _ ->
        val kilometers = distanceMeters.meters().kilometers
        format.format(kilometers)
    }
}

private fun TrackGraphProjection.line(context: Context): LineCartesianLayer.Line =
    LineCartesianLayer.Line(
        fill = LineCartesianLayer.LineFill.single(Fill(context.getColor(color))),
        areaFill = defaultAreaFill(context.getColor(color).toColor()),
        stroke = LineCartesianLayer.LineStroke.continuous(thickness = 1.dp),
    )

// This annotation is needed as long as [TrackGraphProjection] is used as a NavArg
// See https://issuetracker.google.com/issues/358687142
@androidx.annotation.Keep
enum class TrackGraphProjection(
    @ColorRes val color: Int,
    @StringRes val shortName: Int,
    @StringRes val verticalAxisLabel: Int,
    @StringRes val unit: Int
) {
    Speed(R.color.graph_speed, R.string.speed, R.string.legend_speed_kmh, R.string.unit_kmh),
    HeartRate(
        R.color.graph_heart_rate,
        R.string.heart_rate,
        R.string.legend_heart_rate_bpm,
        R.string.unit_bpm
    ),
    Elevation(
        R.color.graph_elevation,
        R.string.Elevation,
        R.string.legend_elevation_m,
        R.string.unit_m
    ),
    Temperature(
        R.color.graph_temperature,
        R.string.temperature,
        R.string.legend_temperature_celsius,
        R.string.unit_celsius
    );

    fun apply(track: Track): Map<Long, Double> {
        val points = track.computedPoints()
        val result = mutableMapOf<Long, Double>()

        for (point in points) {
            mapPoint(point)?.let { result[point.cumDistance.meters.roundToLong()] = it }
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
        Elevation -> ElevationLayerRangeProvider
        HeartRate -> HeartRateLayerRangeProvider
        else -> CartesianLayerRangeProvider.auto()
    }

    private fun mapPoint(point: Track.ComputedTrackPoint): Double? = when (this) {
        Speed -> point.speed.kmh
        HeartRate -> point.point.heartFrequency?.bpm
        Elevation -> point.point.elevation?.meters
        Temperature -> point.point.temperature?.celsius
    }
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
    rangeProvider: CartesianLayerRangeProvider,
    verticalAxisPosition: Axis.Position.Vertical
) : LineCartesianLayer(
    lineProvider,
    rangeProvider = rangeProvider,
    verticalAxisPosition = verticalAxisPosition
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
        pointInfoMap: Map<Double, LineCartesianLayerDrawingModel.Entry>?,
        drawFullLineLength: Boolean,
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
                    layoutDirectionMultiplier * layerDimensions.xSpacing * ((entry.x - minX) / xStep).toFloat()

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
                drawFullLineLength.not() &&
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

/**
 * Alternative to the auto range provider, which usually starts at y-0.
 * That is not very good for elevation graphs, so instead, this class
 * provides a dynamic range.
 */
private class StepLayerRangeProvider(val yStep: Float) : CartesianLayerRangeProvider {
    override fun getMinY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        floor(minY / yStep) * yStep

    override fun getMaxY(minY: Double, maxY: Double, extraStore: ExtraStore) =
        ceil(maxY / yStep) * yStep
}

private val ElevationLayerRangeProvider = StepLayerRangeProvider(100.0f)
private val HeartRateLayerRangeProvider = StepLayerRangeProvider(20f)


class MultiMarkerFormatter(val formatters: List<DefaultCartesianMarker.ValueFormatter>) :
    DefaultCartesianMarker.ValueFormatter {

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        if (targets.size != formatters.size) {
            // FIXME: Figure out which formatting needs to be applied to which targets
            return ""
        }

        val values = targets.zip(formatters).map { (target, formatter) ->
            formatter.format(context, listOf(target))
        }

        val buf = SpannableStringBuilder()
        for ((index, value) in values.withIndex()) {
            buf.append(value)
            if (index < values.lastIndex) {
                buf.append(", ")
            }
        }
        return buf
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
                elevation = Elevation(meters = 0.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 10.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 5.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 10.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 20.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 17.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 6.0)
            ),
            GpxTrackPoint(
                Coordinate(0.0, 0.0),
                time = trackPointTime,
                elevation = Elevation(meters = 0.0)
            ),
        )
    )

    TrackGraph(track, TrackGraphProjection.Elevation, null, modifier = Modifier.fillMaxSize())
}