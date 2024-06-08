package com.inky.fitnesscalendar.ui.views

import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.StatisticsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.compose.common.vicoTheme
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import com.patrykandpatrick.vico.core.common.shape.Shape
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.Locale

@Composable
fun StatisticsView(viewModel: StatisticsViewModel = hiltViewModel(), onOpenDrawer: () -> Unit) {
    val stats by viewModel.activityStatistics.collectAsState(initial = ActivityStatistics(emptyList()))
    StatisticsView(stats, onOpenDrawer)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsView(
    statistics: ActivityStatistics,
    onOpenDrawer: () -> Unit
) {
    val modelProducer = remember { CartesianChartModelProducer.build() }
    var selectedPeriod by remember { mutableStateOf(Period.Week) }

    // TODO: Move this to the view model
    LaunchedEffect(key1 = selectedPeriod, key2 = statistics) {
        val dataPoints =
            selectedPeriod.filter(statistics).map { it.first.activitiesByCategory to it.second }
        if (dataPoints.isNotEmpty()) {
            modelProducer.runTransaction {
                columnSeries {
                    for (category in ActivityCategory.entries) {
                        series(dataPoints.map { it.first[category]?.size ?: 0 })
                    }
                }
                updateExtras { it[labelListKey] = dataPoints.map { point -> point.second } }
            }.await()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.statistics),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.Menu),
                        )
                    }
                },
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        }
    ) { contentPadding ->
        Column(modifier = Modifier.padding(contentPadding)) {
            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                for (period in Period.entries) {
                    DateFilterChip(
                        selected = selectedPeriod == period,
                        onSelect = { selectedPeriod = period },
                        label = { Text(stringResource(period.nameId)) }
                    )
                }
            }
            Graph(modelProducer, stringResource(selectedPeriod.xLabelId))
        }
    }
}

@Composable
fun DateFilterChip(selected: Boolean, onSelect: () -> Unit, label: @Composable () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onSelect,
        label = label,
        leadingIcon = {
            AnimatedVisibility(visible = selected) {
                Icon(Icons.Outlined.Check, stringResource(R.string.time_period))
            }
        },
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun Graph(modelProducer: CartesianChartModelProducer, label: String) {
    val context = LocalContext.current
    val columns = remember {
        ActivityCategory.entries.map { entry ->
            LineComponent(
                color = context.getColor(entry.colorId),
                thicknessDp = 64f,
                shape = Shape.rounded(all = 8.dp)
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize().padding(bottom = 16.dp)
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberColumnCartesianLayer(
                    ColumnCartesianLayer.ColumnProvider.series(columns),
                    mergeMode = { ColumnCartesianLayer.MergeMode.Stacked }
                ),
                startAxis = rememberStartAxis(
                    titleComponent = rememberTextComponent(
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        background = rememberShapeComponent(
                            Shape.Pill,
                            MaterialTheme.colorScheme.secondaryContainer
                        ),
                        padding = Dimensions.of(horizontal = 8.dp, vertical = 2.dp),
                        margins = Dimensions.of(end = 4.dp),
                        typeface = Typeface.MONOSPACE
                    ),
                    title = stringResource(R.string.number_of_activity)
                ),
                bottomAxis = rememberBottomAxis(
                    guideline = null,
                    titleComponent = rememberTextComponent(
                        background = rememberShapeComponent(
                            Shape.Pill,
                            MaterialTheme.colorScheme.secondaryContainer
                        ),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        padding = Dimensions.of(horizontal = 8.dp, vertical = 2.dp),
                        margins = Dimensions.of(top = 4.dp),
                        typeface = Typeface.MONOSPACE
                    ),
                    title = label,
                    valueFormatter = { x, chartValues, _ ->
                        chartValues.model.extraStore[labelListKey][x.toInt()]
                    },
                    itemPlacer = AxisItemPlacer.Horizontal.default(addExtremeLabelPadding = true),
                ),
                legend = rememberLegend(),
            ),
            modelProducer = modelProducer,
            runInitialAnimation = true,
            horizontalLayout = HorizontalLayout.fullWidth(),
            scrollState = rememberVicoScrollState(scrollEnabled = false),
            marker = rememberMarker()
        )
    }
}

@Composable
private fun rememberLegend() =
    rememberHorizontalLegend<CartesianMeasureContext, CartesianDrawContext>(
        items = ActivityCategory.entries.map { category ->
            rememberLegendItem(
                icon = rememberShapeComponent(Shape.Pill, colorResource(category.colorId)),
                label = rememberTextComponent(
                    color = vicoTheme.textColor,
                    textSize = 12.sp,
                    typeface = Typeface.MONOSPACE,
                ),
                labelText = stringResource(category.nameId),
            )
        },
        iconSize = 8.dp,
        iconPadding = 4.dp,
        spacing = 16.dp,
    )


@Composable
private fun rememberMarker() =
    rememberDefaultCartesianMarker(
        label = rememberTextComponent(),
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
    )


enum class Period(val nameId: Int, val xLabelId: Int) {
    Week(R.string.week, R.string.day),
    Month(R.string.month, R.string.week),
    Year(R.string.year, R.string.month),
    All(R.string.all_time, R.string.year);

    fun filter(statistics: ActivityStatistics): List<Pair<ActivityStatistics, String>> {
        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val woyField = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        val dayOfWeekField = WeekFields.of(Locale.getDefault()).dayOfWeek()

        val result: MutableList<Pair<ActivityStatistics?, String>> = mutableListOf()

        val today = LocalDate.now().atStartOfDay()
        // TODO: Filter out older statistics already in the db query
        when (this) {
            Week -> {
                var day = today.minusWeeks(1).plusDays(1)
                val activityMap = statistics.activitiesByDay
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.dayOfYear]) to day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    )
                    day = day.plusDays(1)
                }
            }

            Month -> {
                var day = today.minusMonths(1).with(dayOfWeekField, 1)
                val activityMap = statistics.activitiesByWeek
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.get(woyField)]) to day.format(dateFormatter)
                    )
                    day = day.plusWeeks(1)
                }
            }

            Year -> {
                var day = today.minusYears(1).withDayOfMonth(1).plusMonths(1)
                val activityMap = statistics.activitiesByMonth
                while (!day.isAfter(today)) {
                    result.add(
                        (activityMap[day.monthValue]) to day.month.getDisplayName(
                            TextStyle.SHORT,
                            Locale.getDefault()
                        )
                    )
                    day = day.plusMonths(1)
                }
            }

            All -> {
                val activityMap = statistics.activitiesByYear
                val firstYear = activityMap.keys.min()
                val currentYear = today.year
                for (year in firstYear..currentYear) {
                    result.add((activityMap[year]) to year.toString())
                }
            }
        }

        return result.map { (it.first ?: ActivityStatistics(emptyList())) to it.second }
    }
}

internal val labelListKey = ExtraStore.Key<List<String>>()