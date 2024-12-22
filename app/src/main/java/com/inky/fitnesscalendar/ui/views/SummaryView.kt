package com.inky.fitnesscalendar.ui.views

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
            Text(stringResource(R.string.number_of_activities_n, state.numActivities))
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
    val legendItems: List<Displayable>
) {
    val numActivities get() = statistics.size

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

            return SummaryState(
                statistics = statistics,
                pieChartState = pieChartState,
                legendItems = legendItems
            )
        }
    }
}