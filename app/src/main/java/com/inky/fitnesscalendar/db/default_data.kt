package com.inky.fitnesscalendar.db

import android.content.Context
import androidx.annotation.StringRes
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor

fun loadDefaultData(db: SupportSQLiteDatabase, context: Context) {
    db.beginTransaction()
    try {
        for (type in DefaultActivityType.entries) {
            db.execSQL(
                "INSERT INTO ActivityType(activity_category, name, emoji, color, has_vehicle, has_duration, has_distance, has_intensity) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                arrayOf<Any>(
                    type.activityCategory,
                    context.getString(type.titleId),
                    type.emoji,
                    type.color,
                    type.hasVehicle,
                    type.hasDuration,
                    type.hasDistance,
                    type.hasIntensity
                )
            )
        }

        db.setTransactionSuccessful()
    } finally {
        db.endTransaction()
    }
}

enum class DefaultActivityType(
    val activityCategory: ActivityCategory,
    @StringRes val titleId: Int,
    val emoji: String,
    val color: ContentColor,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
    val hasDistance: Boolean = false,
    val hasIntensity: Boolean = false
) {
    Cycling(
        ActivityCategory.Sports,
        titleId = R.string.activity_cycling,
        color = ContentColor.Color2,
        emoji = "🚴",
        hasDistance = true
    ),
    Running(
        ActivityCategory.Sports,
        titleId = R.string.activity_running,
        color = ContentColor.Color3,
        emoji = "🏃",
        hasDistance = true
    ),
    WorkCommute(
        ActivityCategory.Travel,
        titleId = R.string.activity_commute_home_to_work,
        color = ContentColor.Color1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        titleId = R.string.activity_commute_work_to_home,
        color = ContentColor.Color2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        titleId = R.string.travel,
        color = ContentColor.Color3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(
        ActivityCategory.Work,
        titleId = R.string.work,
        color = ContentColor.Color1,
        emoji = "💼"
    ),
    Gaming(
        ActivityCategory.Entertainment,
        titleId = R.string.gaming,
        color = ContentColor.Color1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        titleId = R.string.film,
        color = ContentColor.Color2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        titleId = R.string.activity_note,
        color = ContentColor.Color1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        titleId = R.string.health_note,
        color = ContentColor.Color2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        titleId = R.string.other,
        color = ContentColor.Color3,
        emoji = "🏷️"
    );
}