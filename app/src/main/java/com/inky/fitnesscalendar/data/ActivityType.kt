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
    UniversityCommute(
        ActivityClass.Travel,
        nameId = R.string.activity_commute_home_to_university,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityClass.Travel,
        nameId = R.string.activity_commute_university_to_home,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityClass.Travel,
        nameId = R.string.travel,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityClass.Work, nameId = R.string.work, emoji = "💼"),
    Note(ActivityClass.Other, nameId = R.string.activity_note, emoji = "📓", hasDuration = false),
    Gaming(ActivityClass.Gaming, nameId = R.string.gaming, emoji = "🎮"),
    Other(ActivityClass.Other, nameId = R.string.other, emoji = "🏷️");

    companion object {
        // A grouping of activity types into rows, where each row contains similar activities
        val BY_ROW: List<List<ActivityType>> = ActivityType.entries.groupBy {
            when (it.activityClass) {
                ActivityClass.Sports -> 0
                ActivityClass.Travel -> 1
                ActivityClass.Gaming, ActivityClass.Work, ActivityClass.Other -> 2
            }
        }.map { (_, types) -> types }.toList()

    }
}