package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.view_model.StatisticsViewModel

data class GraphState(
    val grouping: StatisticsViewModel.FilteredGrouping,
    val period: Period,
    val projection: Projection,
    val statistics: Map<Long, Period.StatisticsEntry>,
) {
    fun diff(newState: GraphState) = Diff(
        mustRefreshActivities = this.period != newState.period || this.grouping != newState.grouping
    )

    data class Diff(val mustRefreshActivities: Boolean)
}