package com.inky.fitnesscalendar.ui.views

import android.content.Context
import android.graphics.Typeface
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.ui.components.CompactActivityCard
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.localDatabaseValues
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.StatisticsViewModel
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.inky.fitnesscalendar.view_model.statistics.Projection
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisGuidelineComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.marker.rememberDefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.data.rememberExtraLambda
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.rememberHorizontalLegend
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.Scroll
import com.patrykandpatrick.vico.core.cartesian.Zoom
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.LegendItem
import com.patrykandpatrick.vico.core.common.component.Shadow
import com.patrykandpatrick.vico.core.common.component.ShapeComponent
import com.patrykandpatrick.vico.core.common.component.TextComponent
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.launch

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
        viewModel = viewModel,
        onOpenDrawer = onOpenDrawer,
        onViewActivity = onViewActivity,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatisticsView(
    viewModel: StatisticsViewModel,
    onOpenDrawer: () -> Unit,
    onViewActivity: (Activity) -> Unit,
) {
    val selectedActivities by viewModel.activityStatistics.collectAsState(initial = emptyMap())

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                colors = defaultTopAppBarColors(),
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
                        grouping = viewModel.grouping,
                        onGrouping = { viewModel.grouping = it }
                    )
                    ProjectionSelectButton(viewModel.projection)
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
                Column(modifier = Modifier.padding(all = 8.dp)) {
                    Text(
                        stringResource(viewModel.projection.legendTextId),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Graph(
                        viewModel.modelProducer,
                        viewModel.projection,
                        viewModel.period,
                        viewModel.groupingOptions.value,
                        modifier = Modifier
                            .fillParentMaxHeight(0.9f)
                            .fillMaxWidth()
                    )
                }
            }

            for ((activities, header) in selectedActivities.values.reversed()) {
                stickyHeader(contentType = ContentType.Date) {
                    Text(
                        header,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                    )
                }

                items(
                    activities.activities,
                    contentType = { ContentType.Activity }) { typeActivity ->
                    CompactActivityCard(
                        richActivity = typeActivity,
                        localizationRepository = viewModel.databaseRepository.localizationRepository,
                        modifier = Modifier.clickable { onViewActivity(typeActivity.activity) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProjectionSelectButton(projection: Projection) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    IconButton(onClick = { menuOpen = true }) {
        Icon(
            painterResource(projection.iconId),
            stringResource(projection.labelTextId)
        )
    }
    DropdownMenu(
        expanded = menuOpen,
        onDismissRequest = { menuOpen = false }) {
        for (projectionEntry in Projection.entries) {
            DropdownMenuItem(
                text = { Text(stringResource(projectionEntry.labelTextId)) },
                leadingIcon = {
                    Icon(
                        painterResource(projectionEntry.iconId),
                        stringResource(R.string.projection)
                    )
                },
                onClick = {
                    scope.launch {
                        Preference.PREF_STATS_PROJECTION.set(context, projectionEntry)
                    }
                    menuOpen = false
                }
            )
        }
    }
}

@Composable
private fun ActivityFilterButton(
    grouping: Grouping,
    onGrouping: (Grouping) -> Unit
) {
    var menuOpen by rememberSaveable { mutableStateOf(false) }

    val filterId =
        if (grouping is Grouping.All) R.drawable.outline_filter_off_24 else R.drawable.outline_filter_24
    IconButton(onClick = { menuOpen = true }) {
        Icon(
            painterResource(filterId),
            stringResource(R.string.filter_activity_category)
        )
    }

    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.filter_all)) }, onClick = {
            menuOpen = false
            onGrouping(Grouping.All)
        })

        HorizontalDivider()

        for (category in ActivityCategory.entries) {
            DropdownMenuItem(
                text = { Text(category.emoji + " " + stringResource(category.nameId)) },
                onClick = {
                    menuOpen = false
                    onGrouping(Grouping.Category(category))
                }
            )
        }

        HorizontalDivider()

        for (type in localDatabaseValues.current.activityTypes) {
            DropdownMenuItem(
                text = { Text(type.emoji + " " + type.name) },
                onClick = {
                    menuOpen = false
                    onGrouping(Grouping.Type(type))
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
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .testTag("DateFilterChip")
    )
}

@Composable
private fun Graph(
    modelProducer: CartesianChartModelProducer,
    projection: Projection,
    period: Period,
    groupingOptions: List<Displayable>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lines = remember(groupingOptions) {
        groupingOptions.map { group ->
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(Fill(group.getColor(context)))
            )
        }
    }

    val scrollState = rememberVicoScrollState(
        initialScroll = Scroll.Absolute.End,
        autoScroll = Scroll.Absolute.End,
        autoScrollCondition = StatisticsViewModel.autoScrollCondition
    )

    CartesianChartHost(
        modifier = modifier,
        chart = rememberCartesianChart(
            rememberLineCartesianLayer(
                LineCartesianLayer.LineProvider.series(lines),
            ),
            startAxis = VerticalAxis.rememberStart(
                itemPlacer = VerticalAxis.ItemPlacer.step({ projection.verticalStepSize }),
                horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
            ),
            bottomAxis = HorizontalAxis.rememberBottom(
                guideline = null,
                titleComponent = rememberTextComponent(
                    background = rememberShapeComponent(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = CorneredShape.Pill,
                    ),
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    padding = dimensions(horizontal = 8.dp, vertical = 2.dp),
                    margins = dimensions(top = 4.dp),
                    typeface = Typeface.MONOSPACE
                ),
                title = stringResource(period.xLabelId),
                valueFormatter = { ctx, value, _ ->
                    ctx.model.extraStore[StatisticsViewModel.xToDateKey][value.toLong()] ?: ""
                },
                itemPlacer = HorizontalAxis.ItemPlacer.aligned(addExtremeLabelPadding = true),
            ),
            legend = rememberLegend(groupingOptions),
            marker = rememberMarker(projection),
        ),
        modelProducer = modelProducer,
        runInitialAnimation = true,
        scrollState = scrollState,
        zoomState = rememberVicoZoomState(initialZoom = remember(period) { Zoom.x(period.numVisibleDays) }),
    )
}

@Composable
private fun rememberLegend(
    groupingOptions: List<Displayable>,
    context: Context = LocalContext.current,
    textColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) =
    rememberHorizontalLegend<CartesianMeasuringContext, CartesianDrawingContext>(
        items = rememberExtraLambda(groupingOptions) {
            groupingOptions.map { group ->
                add(
                    LegendItem(
                        icon = ShapeComponent(
                            shape = CorneredShape.Pill,
                            color = group.getColor(context)
                        ),
                        labelComponent = TextComponent(
                            color = textColor.toArgb(),
                            textSizeSp = 12f,
                            typeface = Typeface.MONOSPACE,
                        ),
                        label = group.getText(context),
                    )
                )
            }
        },
        iconSize = 8.dp,
        iconPadding = 4.dp,
        spacing = 16.dp,
    )


@Composable
private fun rememberMarker(projection: Projection): CartesianMarker =
    rememberDefaultCartesianMarker(
        label = rememberTextComponent(
            color = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        labelPosition = DefaultCartesianMarker.LabelPosition.Top,
        valueFormatter = remember(projection) { projection.markerFormatter() },
        guideline = rememberAxisGuidelineComponent(),
        indicator = { color ->
            ShapeComponent(
                color = color.toArgb(),
                shape = CorneredShape.Pill,
                shadow = Shadow(radiusDp = 12f, color = color.toArgb()),
            )
        }
    )
