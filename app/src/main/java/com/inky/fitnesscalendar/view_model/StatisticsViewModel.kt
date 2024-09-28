package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.inky.fitnesscalendar.view_model.statistics.Projection
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
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
    val databaseRepository: DatabaseRepository
) : ViewModel() {
    var grouping by mutableStateOf<Grouping>(Grouping.All)
    val groupingOptions =
        derivedStateOf { activityTypes.value?.let { grouping.options(it) } ?: emptyList() }
    var period by mutableStateOf(Period.Week)
    var projection by mutableStateOf(Projection.ByTotalActivities)

    private var _activityStatistics = MutableStateFlow<Map<Long, Period.StatisticsEntry>?>(null)
    val activityStatistics get() = _activityStatistics.filterNotNull()

    private var activityTypes = mutableStateOf<List<ActivityType>?>(null)

    val modelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            databaseRepository.getActivityTypes().collect {
                activityTypes.value = it
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            snapshotFlow { period to grouping }.collect { refreshActivities() }
        }
        viewModelScope.launch(Dispatchers.IO) {
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
        val filter = grouping.filter()

        _activityStatistics.value =
            databaseRepository
                .getActivities(filter)
                .shareIn(viewModelScope, SharingStarted.Eagerly)
                .first()
                .let { period.filter(ActivityStatistics(it)) }
    }

    private suspend fun refreshModel() {
        val types = activityTypes.value ?: return

        val dataPoints = _activityStatistics.value?.mapValues { entry ->
            ModelData(
                entryName = entry.value.entryName,
                groups = grouping.apply(entry.value.statistics)
            )
        } ?: return
        if (dataPoints.isEmpty()) return

        val groups = grouping.options(types)
        val groupedDataPoints = groups.map { group ->
            dataPoints.mapNotNull { (key, modelData) ->
                val value = modelData.groups[group]?.let { projection.apply(it) }
                    ?: projection.getDefault()
                value?.let { key to it }
            }.toMap()
        }
        modelProducer.runTransaction {
            // If no data are available, use this hack to clear the graph
            if (groupedDataPoints.all { it.isEmpty() }) {
                lineSeries { series(0) }
            } else {
                lineSeries {
                    for (line in groupedDataPoints) {
                        if (line.isNotEmpty()) {
                            series(x = line.keys, y = line.values)
                        } else {
                            series(0)
                        }
                    }
                }
            }
            extras {
                it[xToDateKey] = dataPoints.mapValues { entry -> entry.value.entryName }
                it[periodKey] = period.ordinal
                it[groupingKey] = grouping
            }
        }
    }

    data class ModelData(
        val entryName: String,
        val groups: Map<out Displayable, ActivityStatistics>
    )

    companion object {
        val xToDateKey = ExtraStore.Key<Map<Long, String>>()
        val periodKey = ExtraStore.Key<Int>()
        val groupingKey = ExtraStore.Key<Grouping>()

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

            if (
                newModel.extraStore.getOrNull(groupingKey)
                != oldModel.extraStore.getOrNull(groupingKey)
            ) {
                return@AutoScrollCondition true
            }

            false
        }
    }
}