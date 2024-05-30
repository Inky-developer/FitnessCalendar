package com.inky.fitnesscalendar.data

data class ActivityStatistics(
    val activities: List<Activity>,
) {
    val activitiesByType: Map<ActivityType, List<Activity>> =
        activities.groupBy { it.type }.toList().sortedBy { (_, v) -> -v.size }.toMap()
    val activitiesByCategory: Map<ActivityCategory, List<Activity>> =
        activities.groupBy { it.type.activityCategory }.toList().sortedBy { (_, v) -> -v.size }
            .toMap()
}