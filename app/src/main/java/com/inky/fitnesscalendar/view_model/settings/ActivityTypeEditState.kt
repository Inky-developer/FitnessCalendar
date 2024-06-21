package com.inky.fitnesscalendar.view_model.settings

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityType

data class ActivityTypeEditState(
    val uid: Int? = null,
    val category: ActivityCategory? = null,
    val name: String = "",
    val emoji: String = "",
    val colorId: Int? = null,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = false,
) {
    constructor(activityType: ActivityType) : this(
        uid = activityType.uid,
        category = activityType.activityCategory,
        name = activityType.name,
        emoji = activityType.emoji,
        colorId = activityType.colorId,
        hasVehicle = activityType.hasVehicle,
        hasDuration = activityType.hasDuration
    )

    val isNewType get() = uid == null


    fun toActivityType(): ActivityType? {
        if (category == null) return null
        if (name.isBlank() || name.lines().size != 1) return null
        // TODO: Make sure this actually is an emoji
        if (emoji.isBlank() || emoji.lines().size != 1) return null
        if (colorId == null) return null

        return ActivityType(
            uid = uid,
            activityCategory = category,
            name = name,
            emoji = emoji,
            colorId = colorId,
            hasVehicle = hasVehicle,
            hasDuration = hasDuration
        )
    }
}