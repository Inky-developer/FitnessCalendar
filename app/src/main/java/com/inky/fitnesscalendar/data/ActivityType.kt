package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class ActivityType(
    val activityClass: ActivityClass,
    val nameId: Int,
    val emoji: String,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(ActivityClass.Sports, nameId = R.string.activity_bouldering, emoji = "🧗"),
    Cycling(ActivityClass.Sports, nameId = R.string.activity_cycling, emoji = "🚴"),
    Running(ActivityClass.Sports, nameId = R.string.activity_running, emoji = "🏃"),
    KungFu(ActivityClass.Sports, nameId = R.string.activity_kung_fu, emoji = "🥋"),
    WorkCommute(
        ActivityClass.Travel,
        nameId = R.string.activity_commute_home_to_work,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityClass.Travel,
        nameId = R.string.activity_commute_work_to_home,
        emoji = "🏡",
        hasVehicle = true
    ),
    Work(ActivityClass.Work, nameId = R.string.work, emoji = "💼"),
    Note(ActivityClass.Other, nameId = R.string.activity_note, emoji = "📓", hasDuration = false);

    companion object {
        val BY_CLASS: Map<ActivityClass, List<ActivityType>> =
            ActivityType.entries.groupBy { it.activityClass }

    }
}