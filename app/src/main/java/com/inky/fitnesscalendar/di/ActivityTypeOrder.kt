package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.TypeActivity

class ActivityTypeOrder(private val frequencies: Map<ActivityType, Int>) {
    fun getRows(typesByCategory: Map<ActivityCategory, List<ActivityType>>) =
        typesByCategory.values.map { types ->
            types.sortedByDescending { frequencies[it] ?: 0 }
        }

    companion object {
        fun init(activities: List<TypeActivity>) = synchronized(this) {
            val frequencies = activities.groupBy { it.type }.mapValues { it.value.size }
            instance = ActivityTypeOrder(frequencies)
        }

        fun getRowsOrDefault(typesByCategory: Map<ActivityCategory, List<ActivityType>>) =
            instance?.getRows(typesByCategory) ?: emptyList()

        var instance: ActivityTypeOrder? = null
    }
}