package com.inky.fitnesscalendar.ui.views

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
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
    val activitiesState = viewModel.repository.getActivities(filter).collectAsState(initial = null)

    when (val activities = activitiesState.value) {
        null -> {}
        else -> SummaryView(activities = activities, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryView(
    activities: List<RichActivity>,
    onBack: () -> Unit,
) {
    val context = LocalContext.current

    val statistics = remember(activities) { ActivityStatistics(activities) }
    val activitiesByType = remember(statistics) { statistics.activitiesByType }
    val pieChartState = remember(activitiesByType) {
        PieChartState(
            activitiesByType.map { (type, value) ->
                PieChartEntry(
                    value = value.size.toDouble(),
                    label = value.size.toString(),
                    color = Color(context.getColor(type.color.colorId))
                )
            }.sortedBy { it.value }
        )
    }

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
            PieChart(pieChartState)
            Legend(activitiesByType)
            Text(stringResource(R.string.number_of_activities_n, activities.size))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Legend(activitiesByType: Map<ActivityType, ActivityStatistics>) {
    FlowRow(modifier = Modifier.padding(vertical = 8.dp)) {
        for (type in activitiesByType.keys) {
            val color = colorResource(type.color.colorId)
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
                Text(type.name)
            }
        }
    }
}