package com.inky.fitnesscalendar.ui.views

import android.graphics.Typeface
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.FilterInformation
import com.inky.fitnesscalendar.ui.components.PieChart
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.components.getAppBarContainerColor
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel
import com.inky.fitnesscalendar.view_model.summary_view.SummaryBoxState
import com.inky.fitnesscalendar.view_model.summary_view.SummaryState
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.shape.CorneredShape

@Composable
fun SummaryView(
    viewModel: BaseViewModel = hiltViewModel(),
    filter: ActivityFilter,
    onBack: () -> Unit,
    onNavigateFilter: () -> Unit,
    onEditFilter: (ActivityFilter) -> Unit
) {
    val activities by viewModel.repository.getActivities(filter).collectAsState(initial = null)
    val context = LocalContext.current

    val dayOfWeekModelProducer = remember { CartesianChartModelProducer() }
    val timeOfDayModelProducer = remember { CartesianChartModelProducer() }
    var state by remember { mutableStateOf<SummaryState?>(null) }
    LaunchedEffect(activities, filter) {
        activities?.let {
            state = SummaryState(
                context = context,
                statistics = ActivityStatistics(it),
                filter = filter,
                dayOfWeekModelProducer = dayOfWeekModelProducer,
                timeOfDayModelProducer = timeOfDayModelProducer
            )
        }
    }

    when (val value = state) {
        null -> {}
        else -> SummaryView(
            state = value,
            onBack = onBack,
            onNavigateFilter = onNavigateFilter,
            onEditFilter = onEditFilter
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryView(
    state: SummaryState,
    onBack: () -> Unit,
    onNavigateFilter: () -> Unit,
    onEditFilter: (ActivityFilter) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.summary)) },
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
                    IconButton(onClick = onNavigateFilter) {
                        Icon(
                            painterResource(R.drawable.outline_filter_24),
                            stringResource(R.string.filter)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(getAppBarContainerColor(scrollBehavior = scrollBehavior))
                    .padding(all = 8.dp)
            ) {
                FilterInformation(filter = state.filter, onChange = onEditFilter)
            }

            AnimatedContent(state.isEmpty, label = "EmptyStateAnimation") { isEmpty ->
                if (isEmpty) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxSize()
                    ) {
                        Icon(
                            Icons.Outlined.Info,
                            stringResource(R.string.info),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .width(32.dp)
                                .aspectRatio(1f)
                                .align(Alignment.CenterVertically)
                        )
                        val textId =
                            if (state.filter.isEmpty()) R.string.no_activities_yet else R.string.no_activities_with_filter
                        Text(
                            stringResource(textId),
                            style = MaterialTheme.typography.displaySmall,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                } else {
                    SummaryViewInner(state = state, onEditFilter = onEditFilter)
                }
            }
        }
    }
}

@Composable
private fun SummaryViewInner(state: SummaryState, onEditFilter: (ActivityFilter) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 128.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        item(key = "PieChart") {
            PieChart(state.pieChartState, onClick = {
                val newFilter = state.handlePieChartClick(it)
                newFilter?.let(onEditFilter)
            })
            AnimatedContent(state.legendItems, label = "LegendItems") { legendItems ->
                Legend(legendItems)
            }
        }

        item(key = "SummaryBox") { SummaryBox(state.summaryBoxState) }
        item(key = "PlaceBox") { PlaceBox(state.places) }

        if (state.feelChartState.dataPoints.size > 1) {
            item(key = "FeelChart") {
                Column(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .animateItem()
                ) {
                    Text(
                        stringResource(R.string.feels),
                        style = MaterialTheme.typography.titleLarge
                    )
                    PieChart(
                        state.feelChartState,
                        onClick = { onEditFilter(state.filter.withFeel(it)) },
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    AnimatedContent(
                        state.feelLegendItems,
                        label = "FeelLegendItems"
                    ) { legendItems ->
                        Legend(legendItems)
                    }
                }
            }
        }

        item(key = "DayOfWeekHistogram") {
            Histogram(
                modelProducer = state.dayOfWeekModelProducer,
                title = stringResource(R.string.Activities_by_weekday),
                xAxisLabel = stringResource(R.string.Weekday)
            )
        }

        item(key = "TimeOfDayHistogram") {
            Histogram(
                modelProducer = state.timeOfDayModelProducer,
                title = stringResource(R.string.Activities_by_time_of_day),
                xAxisLabel = stringResource(R.string.Hour)
            )
        }
    }
}

@Composable
private fun LazyItemScope.PlaceBox(placeStats: Map<Place?, Int>) {
    // Only show place information if there is at least one place that is not null
    if (placeStats.keys.find { it != null } == null) {
        return
    }

    val activitiesWithPlace =
        remember(placeStats) { placeStats.filterKeys { it != null }.values.sum() }

    InfoBox(modifier = Modifier.animateItem()) {
        Text(stringResource(R.string.places))
        SummaryItem(
            stringResource(R.string.summary_activities_with_place),
            activitiesWithPlace.toString()
        )
        SummaryItem(
            stringResource(R.string.summary_activities_without_place),
            (placeStats[null] ?: 0).toString()
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        for ((place, count) in placeStats) {
            if (place == null) {
                continue
            }

            SummaryItem(place.name, count.toString()) {
                CircleIcon(
                    color = colorResource(place.color.colorId),
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryBox(state: SummaryBoxState) {
    InfoBox {
        SummaryItem(stringResource(R.string.summary_total_activities), state.totalActivities)
        SummaryItem(stringResource(R.string.summary_total_time), state.totalTime)
        SummaryItem(stringResource(R.string.summary_average_time), state.averageTime)
        SummaryItem(stringResource(R.string.summary_total_distance), state.totalDistance)
        SummaryItem(stringResource(R.string.summary_average_distance), state.averageDistance)
        SummaryItem(stringResource(R.string.summary_average_speed), state.averageSpeed)
        SummaryItem(
            stringResource(R.string.summary_average_moving_speed),
            state.averageMovingSpeed
        )
        SummaryItem(stringResource(R.string.summary_total_ascent), state.totalAscent)
        SummaryItem(stringResource(R.string.summary_total_descent), state.totalDescent)
        SummaryItem(stringResource(R.string.summary_average_ascent), state.averageAscent)
        SummaryItem(stringResource(R.string.summary_average_descent), state.averageDescent)
        SummaryItem(stringResource(R.string.summary_average_heart_rate), state.averageHeartRate)
        SummaryItem(stringResource(R.string.summary_maximal_heart_rate), state.maximumHeartRate)
        SummaryItem(
            stringResource(R.string.summary_average_temperature),
            state.averageTemperature,
        )
    }
}

@Composable
private fun InfoBox(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        SelectionContainer {
            Column(modifier = Modifier.padding(all = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String?, icon: @Composable () -> Unit = {}) {
    if (value == null) return

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            icon()
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        AnimatedContent(value, label = "SummaryItemValue") { actualValue ->
            Text(actualValue, modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(legendItems: List<Displayable>) {
    FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
        for (item in legendItems) {
            val color = item.color()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                CircleIcon(color = color, modifier = Modifier.padding(end = 2.dp))
                Text(item.text())
            }
        }
    }
}

@Composable
private fun CircleIcon(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(16.dp)
            .background(color)
    )
}

@Composable
private fun Histogram(
    modelProducer: CartesianChartModelProducer,
    title: String,
    xAxisLabel: String
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    columnProvider = ColumnCartesianLayer.ColumnProvider.series(
                        rememberLineComponent(
                            color = colorResource(R.color.graph_default),
                            thickness = 32.dp
                        ),
                    ),
                ),
                startAxis = VerticalAxis.rememberStart(
                    title = stringResource(R.string.number_of_activities),
                    titleComponent = graphTextComponent(),
                    itemPlacer = VerticalAxis.ItemPlacer.step({ 1.0 }),
                    horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    guideline = null,
                    title = xAxisLabel,
                    titleComponent = graphTextComponent(),
                    valueFormatter = { ctx, value, _ ->
                        ctx.model.extraStore[SummaryState.xToLabelKey][value.toInt()]!!
                    }
                ),
            ),
            modelProducer = modelProducer,
            zoomState = rememberVicoZoomState(initialZoom = Zoom.Content, zoomEnabled = false),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            modifier = Modifier.aspectRatio(4 / 3f)
        )
    }
}

@Composable
private fun graphTextComponent() = rememberTextComponent(
    background = rememberShapeComponent(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = CorneredShape.Pill,
        margins = Dimensions(allDp = 2f)
    ),
    color = contentColorFor(MaterialTheme.colorScheme.surfaceContainer),
    padding = dimensions(horizontal = 8.dp, vertical = 4.dp),
    typeface = Typeface.MONOSPACE
)

@Preview
@Composable
fun PreviewSummaryBox() {
    val state = SummaryBoxState(
        totalActivities = "198",
        totalTime = "666h",
        averageTime = "1.5h",
        totalDistance = "12.000km",
        averageDistance = "48km",
        averageSpeed = "30kmh/h",
        averageMovingSpeed = "35km/h",
        totalAscent = "100.000m",
        totalDescent = "100.000m",
        averageAscent = "150m",
        averageDescent = "150m",
        averageHeartRate = "150bpm",
        maximumHeartRate = "190bpm",
        averageTemperature = "15°C"
    )
    SummaryBox(state)
}
