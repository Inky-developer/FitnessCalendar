package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity

class ActivityTypeOrder(private val frequencies: Map<Int?, Int>) {
    fun getRows(typesByCategory: Map<ActivityCategory, List<ActivityType>>) =
        typesByCategory.values.map { types ->
            types.sortedByDescending { frequencies[it.uid] ?: 0 }
        }

    companion object {
        fun init(activities: List<RichActivity>) = synchronized(this) {
            val frequencies = activities.groupBy { it.type.uid }.mapValues { it.value.size }
            instance = ActivityTypeOrder(frequencies)
        }

        fun getRowsOrDefault(typesByCategory: Map<ActivityCategory, List<ActivityType>>) =
            instance?.getRows(typesByCategory) ?: emptyList()

        var instance: ActivityTypeOrder? = null
    }
}