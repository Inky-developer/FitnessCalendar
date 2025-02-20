package com.inky.fitnesscalendar.view_model.summary_view

import android.content.Context
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.measure.Measure
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.util.toLocalDate

data class RecordsBoxState(
    val maximalDuration: Entry?,
    val highestDistance: Entry?,
    val highestAverageMovingSpeed: Entry?,
    val highestAscent: Entry?,
    val highestHeartRate: Entry?
) {
    constructor(context: Context, statistics: ActivityStatistics) :
            this(
                maximalDuration = Entry(
                    context,
                    statistics.maximalDuration()
                ) { it.activity.duration },
                highestDistance = Entry(
                    context,
                    statistics.maximalDistance()
                ) { it.activity.distance },
                highestAverageMovingSpeed = Entry(
                    context,
                    statistics.maximalAverageMovingSpeed()
                ) { it.activity.averageMovingSpeed },
                highestAscent = Entry(
                    context,
                    statistics.maximalAscent()
                ) { it.activity.totalAscent },
                highestHeartRate = Entry(
                    context,
                    statistics.maximalHeartRate()
                ) { it.activity.maximalHeartRate }
            )

    data class Entry(val date: String, val value: String, val activityId: Int) {
        companion object {
            operator fun invoke(
                context: Context,
                activity: RichActivity?,
                mapper: (RichActivity) -> Measure?
            ): Entry? {
                if (activity == null) return null

                val measure = mapper(activity)
                val value = measure?.formatIfNotNothing(context) ?: return null
                return Entry(
                    date = LocalizationRepository.shortLocalDateFormatter.format(activity.activity.startTime.toLocalDate()),
                    value = value,
                    activityId = activity.activity.uid ?: -1
                )
            }
        }
    }
}