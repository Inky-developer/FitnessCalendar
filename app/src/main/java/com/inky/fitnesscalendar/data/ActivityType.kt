package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class ActivityType(
    val activityCategory: ActivityCategory,
    val nameId: Int,
    val emoji: String,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(ActivityCategory.Sports, nameId = R.string.activity_bouldering, emoji = "🧗"),
    Cycling(ActivityCategory.Sports, nameId = R.string.activity_cycling, emoji = "🚴"),
    Running(ActivityCategory.Sports, nameId = R.string.activity_running, emoji = "🏃"),
    KungFu(ActivityCategory.Sports, nameId = R.string.activity_kung_fu, emoji = "🥋"),
    UniversityCommute(
        ActivityCategory.Travel,
        nameId = R.string.activity_commute_home_to_university,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        nameId = R.string.activity_commute_university_to_home,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        nameId = R.string.travel,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, nameId = R.string.work, emoji = "💼"),
    Gaming(ActivityCategory.Entertainment, nameId = R.string.gaming, emoji = "🎮"),
    Note(ActivityCategory.Other, nameId = R.string.activity_note, emoji = "📓", hasDuration = false),
    Other(ActivityCategory.Other, nameId = R.string.other, emoji = "🏷️");

    fun hasFeel() = hasDuration

    companion object {
        // A grouping of activity types into rows, where each row contains similar activities
        val BY_ROW: List<List<ActivityType>> = ActivityType.entries.groupBy {
            when (it.activityCategory) {
                ActivityCategory.Sports -> 0
                ActivityCategory.Travel -> 1
                ActivityCategory.Entertainment, ActivityCategory.Work, ActivityCategory.Other -> 2
            }
        }.map { (_, types) -> types }.toList()

    }
}