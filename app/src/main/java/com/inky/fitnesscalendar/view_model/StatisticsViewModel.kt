package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.AppRepository
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.inky.fitnesscalendar.view_model.statistics.Projection
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val appRepository: AppRepository
) : ViewModel() {
    var grouping by mutableStateOf(Grouping(null))
    val groupingOptions =
        derivedStateOf { activityTypes?.let { grouping.options(it) } ?: emptyList() }
    var period by mutableStateOf(Period.Week)
    var projection by mutableStateOf(Projection.ByTotalActivities)

    private var _activityStatistics =
        MutableStateFlow<List<Pair<ActivityStatistics, String>>?>(null)
    val activityStatistics get() = _activityStatistics.filterNotNull()

    private var activityTypes: List<ActivityType>? by mutableStateOf(null)

    var numDataPoints by mutableIntStateOf(0)

    val modelProducer = CartesianChartModelProducer.build()

    init {
        viewModelScope.launch {
            appRepository.getActivityTypes().collect {
                activityTypes = it
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { period to grouping }.collect { refreshActivities() }
        }
        viewModelScope.launch {
            snapshotFlow { projection to activityTypes }.collect { refreshModel() }
        }

        activityStatistics.onEach {
            refreshModel()
        }.launchIn(viewModelScope)

        Preference.PREF_STATS_PROJECTION.flow(context)
            .onEach { projection = it }
            .launchIn(viewModelScope)
    }

    private suspend fun refreshActivities() {
        val filter = ActivityFilter(categories = listOfNotNull(grouping.category))

        _activityStatistics.value =
            appRepository
                .getActivities(filter)
                .shareIn(viewModelScope, SharingStarted.Eagerly)
                .first()
                .let { ActivityStatistics(it) }
                .let { period.filter(it) }
    }

    private suspend fun refreshModel() {
        val types = activityTypes ?: return

        val dataPoints =
            _activityStatistics.value?.map { grouping.apply(it.first) to it.second } ?: return
        numDataPoints = dataPoints.size
        if (dataPoints.isEmpty()) return

        modelProducer.runTransaction {
            columnSeries {
                for (group in grouping.options(types)) {
                    series(dataPoints.map { (stats, _) ->
                        stats[group]?.let { projection.apply(it) } ?: 0
                    })
                }
            }
            updateExtras {
                it[labelListKey] = dataPoints.map { (_, label) -> label }
                it[periodKey] = period.ordinal
            }
        }.await()
    }

    companion object {
        val labelListKey = ExtraStore.Key<List<String>>()
        val periodKey = ExtraStore.Key<Int>()

        val autoScrollCondition = AutoScrollCondition { newModel, oldModel ->
            if (oldModel == null) {
                return@AutoScrollCondition true
            }

            if (newModel.models.size != oldModel.models.size) {
                return@AutoScrollCondition true
            }

            if (newModel.extraStore.getOrNull(periodKey) != oldModel.extraStore.getOrNull(periodKey)) {
                return@AutoScrollCondition true
            }

            false
        }
    }
}