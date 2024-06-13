package com.inky.fitnesscalendar.data

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R

enum class ActivityType(
    val activityCategory: ActivityCategory,
    @StringRes val nameId: Int,
    val emoji: String,
    @ColorRes val colorId: Int,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) : Displayable {
    Bouldering(
        ActivityCategory.Sports,
        nameId = R.string.activity_bouldering,
        colorId = R.color.stats_1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        nameId = R.string.activity_cycling,
        colorId = R.color.stats_2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        nameId = R.string.activity_running,
        colorId = R.color.stats_3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        nameId = R.string.activity_kung_fu,
        colorId = R.color.stats_4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        nameId = R.string.activity_commute_home_to_university,
        colorId = R.color.stats_1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        nameId = R.string.activity_commute_university_to_home,
        colorId = R.color.stats_2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        nameId = R.string.travel,
        colorId = R.color.stats_3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, nameId = R.string.work, colorId = R.color.stats_1, emoji = "💼"),
    Gaming(
        ActivityCategory.Entertainment,
        nameId = R.string.gaming,
        colorId = R.color.stats_1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        nameId = R.string.film,
        colorId = R.color.stats_2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        nameId = R.string.activity_note,
        colorId = R.color.stats_1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        nameId = R.string.health_note,
        colorId = R.color.stats_2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(ActivityCategory.Other, nameId = R.string.other, colorId = R.color.stats_3, emoji = "🏷️");

    fun hasFeel() = hasDuration

    override fun getColor(context: Context) = context.getColor(colorId)

    override fun getText(context: Context) = context.getString(nameId)

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