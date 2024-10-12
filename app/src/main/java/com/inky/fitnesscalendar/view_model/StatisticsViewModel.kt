package com.inky.fitnesscalendar.view_model

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.view_model.statistics.GraphState
import com.inky.fitnesscalendar.view_model.statistics.Grouping
import com.inky.fitnesscalendar.view_model.statistics.Period
import com.patrykandpatrick.vico.core.cartesian.AutoScrollCondition
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
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
    private var _graphState = MutableStateFlow<GraphState?>(null)
    val graphState get() = _graphState.asStateFlow()

    private var initialPeriod: Period = Period.Week

    val modelProducer = CartesianChartModelProducer()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            val projection = Preference.PREF_STATS_PROJECTION.get(context)
            _graphState.value = GraphState(
                grouping = FilteredGrouping(Grouping.All),
                period = Period.Week,
                projection = projection,
                statistics = emptyMap()
            )

            refreshActivities()
        }

        Preference.PREF_STATS_PROJECTION.flow(context).onEach { projection ->
            val state = graphState.value
            if (state != null && state.projection != projection) {
                updateState(state.copy(projection = projection))
            }
        }.launchIn(viewModelScope)
    }

    fun setPeriod(period: Period) {
        val state = graphState.value
        if (state != null) {
            updateState(state.copy(period = period))
        } else {
            initialPeriod = period
        }
    }

    fun setGrouping(grouping: Grouping) {
        val newGrouping = if (grouping is FilteredGrouping) {
            grouping
        } else {
            FilteredGrouping(grouping)
        }
        graphState.value?.copy(grouping = newGrouping)?.let { updateState(it) }
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

        val filter = state.grouping.filter()
        viewModelScope.launch(Dispatchers.IO) {
            val statistics = databaseRepository
                .getActivities(filter)
                .shareIn(viewModelScope, SharingStarted.Eagerly)
                .first()
            val statisticsMap = state.period.filter(ActivityStatistics(statistics))
            _graphState.value = state.copy(statistics = statisticsMap)
            refreshModel()
        }
    }

    private suspend fun refreshModel() {
        val state = _graphState.value ?: return
        val dataPoints = state.statistics.mapValues { entry ->
            ModelData(
                entryName = entry.value.entryName,
                groups = state.grouping.apply(entry.value.statistics)
            )
        }
        if (dataPoints.isEmpty()) return

        val groups = state.grouping.options()
        val groupedDataPoints = groups.map { group ->
            dataPoints.mapNotNull { (key, modelData) ->
                val value = modelData.groups[group]?.let { state.projection.apply(it) }
                    ?: state.projection.getDefault()
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
                it[periodKey] = state.period.ordinal
                it[groupingKey] = state.grouping
            }
        }
    }

    data class ModelData(
        val entryName: String,
        val groups: Map<out Displayable, ActivityStatistics>
    )

    data class FilteredGrouping(
        val grouping: Grouping,
        val filteredIndexes: Set<Int> = emptySet()
    ) : Grouping by grouping {
        fun withIndex(index: Int): FilteredGrouping {
            val options = grouping.options()
            // Don't allow filtering every index, because vico would crash otherwise :(
            if (options.size == filteredIndexes.size + 1) {
                return this
            }
            return copy(filteredIndexes = filteredIndexes + index)
        }

        fun withoutIndex(index: Int) = copy(filteredIndexes = filteredIndexes - index)

        // Sets all groups that are filtered out to 0
        override fun apply(statistics: ActivityStatistics): Map<out Displayable, ActivityStatistics> {
            val unfilteredMap = grouping.apply(statistics)
            val validGroups =
                options().toSet().filterIndexed { index, _ -> !filteredIndexes.contains(index) }

            return unfilteredMap.mapValues {
                if (validGroups.contains(it.key)) {
                    it.value
                } else {
                    ActivityStatistics(emptyList())
                }
            }
        }
    }

    companion object {
        val xToDateKey = ExtraStore.Key<Map<Long, String>>()
        val periodKey = ExtraStore.Key<Int>()
        val groupingKey = ExtraStore.Key<FilteredGrouping>()

        val autoScrollCondition = AutoScrollCondition { newModel, oldModel ->
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
                newModel.extraStore.getOrNull(groupingKey)?.grouping
                != oldModel.extraStore.getOrNull(groupingKey)?.grouping
            ) {
                return@AutoScrollCondition true
            }

            false
        }
    }
}