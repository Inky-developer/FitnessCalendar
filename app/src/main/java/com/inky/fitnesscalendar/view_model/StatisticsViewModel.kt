package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.view_model.statistics.GraphState
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.inky.fitnesscalendar.view_model.statistics.Projection
import com.patrykandpatrick.vico.compose.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.compose.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.compose.cartesian.data.lineModel
import com.patrykandpatrick.vico.compose.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    val app: AppRepository
) : ViewModel() {
    private var _graphState = MutableStateFlow<GraphState?>(null)
    val graphState get() = _graphState.asStateFlow()

    private var initialPeriod: Period = Period.Week
    private var initialFilter: ActivityFilter = ActivityFilter()

    val modelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val projection = Preference.PREF_STATS_PROJECTION.get(context)
            _graphState.value = GraphState(
                grouping = Grouping.All,
                groups = emptyList(),
                period = initialPeriod,
                filter = initialFilter,
                projection = projection,
                statistics = emptyMap(),
            )

            refreshActivities()
        }
    }

    fun setProjection(projection: Projection) {
        val state = graphState.value
        if (state != null && state.projection != projection) {
            updateState(state.copy(projection = projection))
        }
    }

    fun setPeriod(period: Period) {
        val state = graphState.value
        if (state != null) {
            updateState(state.copy(period = period))
        } else {
            initialPeriod = period
        }
    }

    fun setFilter(filter: ActivityFilter) {
        val state = graphState.value
        if (state != null) {
            updateState(state.copy(filter = filter))
        } else {
            initialFilter = filter
        }
    }

    fun setGrouping(grouping: Grouping) {
        graphState.value?.copy(grouping = grouping)?.let(::updateState)
    }

    fun toggleGroup(index: Int) {
        val groups = graphState.value?.groups?.toMutableList() ?: return
        groups.getOrNull(index) ?: return
        groups[index] = groups[index].copy(enabled = !groups[index].enabled)
        graphState.value?.copy(groups = groups)?.let(::updateState)
    }

    private fun updateState(newState: GraphState) {
        val oldState = graphState.value ?: return
        val diff = oldState.diff(newState)

        _graphState.value = newState

        if (diff.mustRefreshActivities) {
            refreshActivities()
        } else {
            synchronized(this) {
                viewModelScope.launch(Dispatchers.Default) {
                    refreshModel()
                }
            }
        }
    }

    private fun refreshActivities() = synchronized(this) {
        val state = _graphState.value ?: return@synchronized

        val filter =
            state.grouping.filterCategory()?.let { state.filter.withCategory(it) } ?: state.filter
        viewModelScope.launch(Dispatchers.IO) {
            val statistics = app.db
                .getActivities(filter)
                .shareIn(viewModelScope, SharingStarted.Eagerly)
                .first()
            val statisticsMap = state.period.filter(ActivityStatistics(statistics))
            _graphState.value = state.copy(
                statistics = statisticsMap,
                groups = state.grouping.apply(ActivityStatistics(statistics))
            )
            refreshModel()
        }
    }

    private suspend fun refreshModel() {
        val state = _graphState.value ?: return
        val dataPoints = state.statistics.mapValues { entry ->
            ModelData(
                entryName = entry.value.entryName,
                groups = state.grouping.apply(entry.value.statistics)
                    .associate { it.value to it.stats }
            )
        }

        val groupedDataPoints = state.groups.map { group ->
            // Disabled groups keep their line (so line/series counts stay aligned) but render empty
            if (!group.enabled) {
                emptyMap()
            } else {
                dataPoints.mapNotNull { (key, modelData) ->
                    val value = modelData.groups[group.value]?.let { state.projection.apply(it) }
                        ?: state.projection.getDefault()
                    value?.let { key to it }
                }.toMap()
            }
        }

        if (groupedDataPoints.isEmpty()) {
            return
        }

        modelProducer.runTransaction {
            lineModel {
                for (line in groupedDataPoints) {
                    if (line.isNotEmpty()) {
                        series(x = line.keys, y = line.values)
                    } else {
                        series(0)
                    }
                }
            }
            extras {
                it[xToDateKey] = dataPoints.mapValues { entry -> entry.value.entryName }
                it[periodKey] = state.period.ordinal
                it[groupingKey] = state.grouping
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

        val autoScrollCondition = AutoScrollCondition { oldModel, newModel ->
            if (oldModel == null) {
                return@AutoScrollCondition true
            }

            if (newModel.models.size != oldModel.models.size) {
                return@AutoScrollCondition true
            }

            if (newModel.extraStore.getOrNull(periodKey) != oldModel.extraStore.getOrNull(
                    periodKey
                )
            ) {
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