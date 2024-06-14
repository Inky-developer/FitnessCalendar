package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.StatisticsViewModel
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.inky.fitnesscalendar.view_model.statistics.Projection
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.fullWidth
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.of
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.compose.common.rememberLegendItem
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasureContext
import com.patrykandpatrick.vico.core.cartesian.HorizontalLayout
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Dimensions
import com.patrykandpatrick.vico.core.common.component.LineComponent
import com.patrykandpatrick.vico.core.common.shape.Shape

@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel = hiltViewModel(),
    initialPeriod: Period? = null,
    onOpenDrawer: () -> Unit,
    onViewActivity: (Activity) -> Unit
) {
    if (initialPeriod != null) {
        viewModel.period = initialPeriod
    }

    StatisticsView(
        onOpenDrawer = onOpenDrawer,
        onViewActivity = onViewActivity,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel = hiltViewModel(),
    onOpenDrawer: () -> Unit,
    onViewActivity: (Activity) -> Unit,
) {
    val selectedActivities by viewModel.activityStatistics.collectAsState(initial = emptyList())

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.statistics),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    scrolledContainerColor = MaterialTheme.colorScheme.primaryContainer,
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
                actions = {
                    ActivityFilterButton(
                        selectedCategory = viewModel.grouping.category,
                        onCategory = { viewModel.grouping = Grouping(it) }
                    )
                    ProjectionSelectButton(viewModel.projection) { viewModel.projection = it }
                },
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { contentPadding ->
        LazyColumn(modifier = Modifier.padding(contentPadding)) {
            stickyHeader(contentType = ContentType.Tabs) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    for (period in Period.entries) {
                        DateFilterChip(
                            selected = viewModel.period == period,
                            onSelect = { viewModel.period = period },
                            label = { Text(stringResource(period.nameId)) }
                        )
                    }
                }
            }

            item(contentType = ContentType.Graph) {
                Graph(
                    viewModel.modelProducer,
                    viewModel.projection,
                    viewModel.period,
                    viewModel.grouping,
                    modifier = Modifier
                        .fillParentMaxHeight(0.9f)
                        .fillMaxWidth()
                )
            }

            for ((activities, header) in selectedActivities.asReversed()) {
                stickyHeader(contentType = ContentType.Date) {
                    Text(
                        header,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(all = 8.dp)
                            .fillMaxWidth()
                    )
                }

                items(activities.activities, contentType = { ContentType.Activity }) { activity ->
                    CompactActivityCard(
                        activity = activity,
                        localizationRepository = viewModel.appRepository.localizationRepository,
                        modifier = Modifier.clickable { onViewActivity(activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectionSelectButton(projection: Projection, onProjection: (Projection) -> Unit) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    IconButton(onClick = { menuOpen = true }) {
        when (projection) {
            Projection.ByTotalTime -> Icon(
                painterResource(R.drawable.outline_total_time_24),
                stringResource(R.string.by_total_time)
            )

            Projection.ByAverageTime -> Icon(
                painterResource(R.drawable.outline_timer_24),
                stringResource(R.string.by_average_time)
            )

            Projection.ByTotalActivities -> Icon(
                painterResource(R.drawable.outline_numbers_24),
                stringResource(R.string.number_of_activities)
            )
        }
    }
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.by_total_activities)) },
            onClick = {
                onProjection(Projection.ByTotalActivities)
                menuOpen = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.by_total_time)) },
            onClick = {
                onProjection(Projection.ByTotalTime)
                menuOpen = false
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.by_average_time)) },
            onClick = {
                onProjection(Projection.ByAverageTime)
                menuOpen = false
            }
        )
    }
}

@Composable
private fun ActivityFilterButton(
    selectedCategory: ActivityCategory?,
    onCategory: (ActivityCategory?) -> Unit
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    val filterId =
        if (selectedCategory == null) R.drawable.outline_filter_off_24 else R.drawable.outline_filter_24
    IconButton(onClick = { menuOpen = true }) {
        Icon(
            painterResource(filterId),
            stringResource(R.string.filter_activity_category)
        )
    }

    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.filter_all)) }, onClick = {
            menuOpen = false
            onCategory(null)
        })

        for (category in ActivityCategory.entries) {
            DropdownMenuItem(
                text = { Text(category.emoji + " " + stringResource(category.nameId)) },
                onClick = {
                    menuOpen = false
                    onCategory(category)
                }
            )
        }
    }
}

private enum class ContentType {
    Tabs,
    Graph,
    Date,
    Activity
}

@Composable
private fun DateFilterChip(selected: Boolean, onSelect: () -> Unit, label: @Composable () -> Unit) {
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
private fun Graph(
    modelProducer: CartesianChartModelProducer,
    projection: Projection,
    period: Period,
    grouping: Grouping,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val columns = remember(grouping) {
        grouping.options().map { group ->
            LineComponent(
                color = group.getColor(context),
                thicknessDp = 64f,
                shape = Shape.rounded(all = 8.dp)
            )
        }
    }

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = AutoScrollCondition.OnModelSizeIncreased
    )

    Surface(
        modifier = modifier
            .padding(bottom = 16.dp)
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
                    title = stringResource(projection.legendTextId),
                    itemPlacer = AxisItemPlacer.Vertical.step({ projection.verticalStepSize() })
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
                    title = stringResource(period.xLabelId),
                    valueFormatter = { x, chartValues, _ ->
                        chartValues.model.extraStore[StatisticsViewModel.labelListKey][x.toInt()]
                    },
                    itemPlacer = AxisItemPlacer.Horizontal.default(addExtremeLabelPadding = true),
                ),
                legend = rememberLegend(grouping),
            ),
            modelProducer = modelProducer,
            runInitialAnimation = true,
            horizontalLayout = HorizontalLayout.fullWidth(),
            scrollState = scrollState,
            // TODO: use rememberSaveable
            zoomState = rememberVicoZoomState(initialZoom = remember(period) { Zoom.x(period.numVisibleEntries + 0.5f) }),
            marker = rememberMarker()
        )
    }
}

@Composable
private fun rememberLegend(grouping: Grouping, context: Context = LocalContext.current) =
    rememberHorizontalLegend<CartesianMeasureContext, CartesianDrawContext>(
        items = grouping.options().map { group ->
            rememberLegendItem(
                icon = rememberShapeComponent(Shape.Pill, Color(group.getColor(context))),
                label = rememberTextComponent(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textSize = 12.sp,
                    typeface = Typeface.MONOSPACE,
                ),
                labelText = group.getText(context),
            )
        },
        iconSize = 8.dp,
        iconPadding = 4.dp,
        spacing = 16.dp,
    )


@Composable
private fun rememberMarker() =
    rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
    )
