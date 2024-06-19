package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.TypeActivity

class ActivityTypeOrder(val typesByRow: List<List<ActivityType>>) {
    companion object {
        fun init(activities: List<TypeActivity>, types: List<ActivityType>) = synchronized(this) {
            val frequencies = activities.groupBy { it.type }.mapValues { it.value.size }
            val typesByCategory = types.groupBy { it.activityCategory }
            val rows = typesByCategory.values.map { types ->
                types.sortedByDescending { frequencies[it] ?: 0 }
            }
            instance = ActivityTypeOrder(rows)
        }

        fun getRows() = instance?.typesByRow ?: emptyList()

        var instance: ActivityTypeOrder? = null
    }
}