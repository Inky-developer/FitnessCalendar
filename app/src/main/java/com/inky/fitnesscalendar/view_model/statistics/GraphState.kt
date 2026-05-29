package com.inky.fitnesscalendar.view_model.statistics

import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter

data class GraphState(
    val grouping: Grouping,
    val groups: List<Grouping.Group>,
    val filter: ActivityFilter,
    val period: Period,
    val projection: Projection,
    val statistics: Map<Long, Period.StatisticsEntry>,
) {
    fun diff(newState: GraphState) = Diff(
        mustRefreshActivities = this.period != newState.period || this.grouping != newState.grouping || this.filter != newState.filter
    )

    data class Diff(val mustRefreshActivities: Boolean)
}