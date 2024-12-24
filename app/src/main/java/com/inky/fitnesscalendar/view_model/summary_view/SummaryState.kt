package com.inky.fitnesscalendar.view_model.summary_view

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.Displayable
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.ui.components.PieChartEntry
import com.inky.fitnesscalendar.ui.components.PieChartState
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.common.data.ExtraStore
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

data class SummaryState internal constructor(
    private val statistics: ActivityStatistics,
    val filter: ActivityFilter,
    val pieChartState: PieChartState<Displayable>,
    val legendItems: List<Displayable>,
    val summaryBoxState: SummaryBoxState,
    val places: Map<Place?, Int>,
    val feelChartState: PieChartState<Feel>,
    val feelLegendItems: List<Feel>,
    val dayOfWeekModelProducer: CartesianChartModelProducer,
    val timeOfDayModelProducer: CartesianChartModelProducer
) {
    fun handlePieChartClick(element: Any): ActivityFilter? {
        return when (element) {
            is ActivityType -> filter.withType(element)
            is ActivityCategory -> filter.withCategory(element)
            else -> null
        }
    }

    companion object {
        suspend operator fun invoke(
            context: Context,
            statistics: ActivityStatistics,
            filter: ActivityFilter,
            dayOfWeekModelProducer: CartesianChartModelProducer,
            timeOfDayModelProducer: CartesianChartModelProducer
        ): SummaryState {
            val activitiesByCategory = statistics.activitiesByCategory
            val isSingleCategory = activitiesByCategory.size == 1

            val chartStats = if (isSingleCategory) {
                statistics.activitiesByType
            } else {
                statistics.activitiesByCategory
            }
            val pieChartState = PieChartState(
                chartStats.map { (key, value) ->
                    PieChartEntry(
                        value = value.size.toDouble(),
                        label = value.size.toString(),
                        color = Color(key.getColor(context)),
                        payload = key
                    )
                }
            )
            val legendItems = chartStats.toList().sortedBy { (_, v) -> -v.size }.map { (k, _) -> k }

            val summaryBoxState = SummaryBoxState(context, statistics)

            val places = statistics.activitiesByPlace.mapValues { it.value.size }

            val feelChartState = PieChartState(statistics.activitiesByFeel.map { (key, value) ->
                PieChartEntry(
                    value = value.size.toDouble(),
                    label = value.size.toString(),
                    color = Color(key.getColor(context)),
                    payload = key
                )
            })
            val feelLegendItems = Feel.entries.reversed()

            dayOfWeekModelProducer.apply {
                val weekdayStats =
                    statistics.activitiesByWeekday.toSortedMap().mapValues { it.value.size }
                runTransaction {
                    columnSeries {
                        series(DayOfWeek.entries.map { weekdayStats[it] ?: 0 })
                    }
                    extras {
                        val locale = Locale.getDefault()
                        it[xToLabelKey] =
                            DayOfWeek.entries.withIndex().associate { (index, value) ->
                                index to value.getDisplayName(TextStyle.SHORT, locale)
                            }
                    }
                }
            }

            timeOfDayModelProducer.apply {
                val hourOfDayStats =
                    statistics.activitiesByHourOfDay.toSortedMap().mapValues { it.value.size }
                runTransaction {
                    columnSeries {
                        series((0..<24).map { hourOfDayStats[it] ?: 0 })
                    }
                    extras {
                        it[xToLabelKey] = (0..<24).associateWith { hour -> hour.toString() }
                    }
                }
            }

            return SummaryState(
                statistics = statistics,
                filter = filter,
                pieChartState = pieChartState,
                legendItems = legendItems,
                summaryBoxState = summaryBoxState,
                places = places,
                feelChartState = feelChartState,
                feelLegendItems = feelLegendItems,
                dayOfWeekModelProducer = dayOfWeekModelProducer,
                timeOfDayModelProducer = timeOfDayModelProducer
            )
        }

        internal val xToLabelKey = ExtraStore.Key<Map<Int, String>>()
    }
}