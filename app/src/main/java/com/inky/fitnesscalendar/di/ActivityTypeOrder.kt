package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityType

class ActivityTypeOrder(val typesByRow: List<List<ActivityType>>) {
    companion object {
        fun init(activities: List<Activity>) = synchronized(this) {
            val frequencies = activities.groupBy { it.type }.mapValues { it.value.size }
            val rows = ActivityType.BY_ROW.map { row ->
                row.sortedByDescending { frequencies[it] ?: 0 }
            }
            instance = ActivityTypeOrder(rows)
        }

        var instance: ActivityTypeOrder? = null
    }
}