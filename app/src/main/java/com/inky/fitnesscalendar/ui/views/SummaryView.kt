package com.inky.fitnesscalendar.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.ui.components.PieChart
import com.inky.fitnesscalendar.ui.components.PieChartEntry
import com.inky.fitnesscalendar.ui.components.PieChartState
import com.inky.fitnesscalendar.ui.components.defaultTopAppBarColors
import com.inky.fitnesscalendar.ui.util.SharedContentKey
import com.inky.fitnesscalendar.ui.util.sharedBounds
import com.inky.fitnesscalendar.view_model.BaseViewModel

@Composable
fun SummaryView(
    viewModel: BaseViewModel = hiltViewModel(),
    filter: ActivityFilter,
    onBack: () -> Unit,
) {
    val activities by viewModel.repository.getActivities(filter).collectAsState(initial = null)
    val context = LocalContext.current
    val state = remember(activities, filter) {
        activities?.let {
            SummaryState(
                context,
                ActivityStatistics(it),
                filter
            )
        }
    }

    when (state) {
        null -> {}
        else -> SummaryView(state = state, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryView(
    state: SummaryState,
    onBack: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
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
                scrollBehavior = scrollBehavior,
                modifier = Modifier.sharedBounds(SharedContentKey.AppBar)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            PieChart(state.pieChartState)
            Legend(state.legendItems)

            SummaryBox(state.summaryBoxState)

            Spacer(modifier = Modifier.height(128.dp))
        }
    }
}

@Composable
private fun SummaryBox(state: SummaryBoxState) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(all = 8.dp)) {
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
            SummaryItem(stringResource(R.string.summary_average_heart_rate), state.averageHeartRate)
            SummaryItem(stringResource(R.string.summary_maximal_heart_rate), state.maximumHeartRate)
            SummaryItem(
                stringResource(R.string.summary_average_temperature),
                state.averageTemperature,
                showDivider = false
            )
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String?, showDivider: Boolean = true) {
    if (value == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )

            Text(value, modifier = Modifier.weight(1f))
        }
        if (showDivider) {
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
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
                Box(
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .clip(CircleShape)
                        .size(16.dp)
                        .background(color)
                )
                Text(item.text())
            }
        }
    }
}

data class SummaryState internal constructor(
    private val statistics: ActivityStatistics,
    val pieChartState: PieChartState,
    val legendItems: List<Displayable>,
    val summaryBoxState: SummaryBoxState,
) {
    companion object {
        operator fun invoke(
            context: Context,
            statistics: ActivityStatistics,
            filter: ActivityFilter
        ): SummaryState {
            val singleCategory = filter.categories.singleOrNull()

            val chartStats = if (singleCategory != null) {
                statistics.activitiesByType
            } else {
                statistics.activitiesByCategory
            }
            val pieChartState = PieChartState(
                chartStats.map { (key, value) ->
                    PieChartEntry(
                        value = value.size.toDouble(),
                        label = value.size.toString(),
                        color = Color(key.getColor(context))
                    )
                }
            )
            val legendItems = chartStats.toList().sortedBy { (_, v) -> v.size }.map { (k, _) -> k }

            val summaryBoxState = SummaryBoxState(context, statistics)

            return SummaryState(
                statistics = statistics,
                pieChartState = pieChartState,
                legendItems = legendItems,
                summaryBoxState = summaryBoxState
            )
        }
    }
}

data class SummaryBoxState(
    val totalActivities: String,
    val totalTime: String?,
    val averageTime: String?,
    val totalDistance: String?,
    val averageDistance: String?,
    val averageSpeed: String?,
    val averageMovingSpeed: String?,
//    val maximumSpeed: String?,
//    val minimumElevation: String?,
//    val maximumElevation: String?,
    val totalAscent: String?,
    val totalDescent: String?,
    val averageHeartRate: String?,
    val maximumHeartRate: String?,
//    val minimumTemperature: String?,
//    val maximumTemperature: String?,
    val averageTemperature: String?,
) {
    constructor(context: Context, statistics: ActivityStatistics) : this(
        totalActivities = statistics.size.toString(),
        totalTime = statistics.totalTime().takeIf { it.elapsedMs > 0L }?.format(),
        averageTime = statistics.averageTime()?.format(),
        totalDistance = statistics.totalDistance().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        averageDistance = statistics.averageDistance()?.formatWithContext(context),
        averageSpeed = statistics.averageSpeed()?.formatWithContext(context),
        averageMovingSpeed = statistics.averageMovingSpeed()?.formatWithContext(context),
        totalAscent = statistics.totalAscent().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        totalDescent = statistics.totalDescent().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        averageHeartRate = statistics.averageHeartRate()?.formatWithContext(context),
        maximumHeartRate = statistics.maximalHeartRate()?.formatWithContext(context),
        averageTemperature = statistics.averageTemperature()?.formatWithContext(context),
    )

}

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
        averageHeartRate = "150bpm",
        maximumHeartRate = "190bpm",
        averageTemperature = "15°C"
    )
    SummaryBox(state)
}
