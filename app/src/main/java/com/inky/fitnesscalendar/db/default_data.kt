package com.inky.fitnesscalendar.db

import android.content.Context
import androidx.annotation.StringRes
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityTypeColor

// TODO: Add test for this
fun loadDefaultData(db: SupportSQLiteDatabase, context: Context) {
    db.beginTransaction()
    try {
        for (type in DefaultActivityType.entries) {
            db.execSQL(
                "INSERT INTO ActivityType(activity_category, name, emoji, color, has_vehicle, has_duration) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(
                    type.activityCategory,
                    context.getString(type.titleId),
                    type.emoji,
                    type.color,
                    type.hasVehicle,
                    type.hasDuration
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
    val color: ActivityTypeColor,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(
        ActivityCategory.Sports,
        titleId = R.string.activity_bouldering,
        color = ActivityTypeColor.Color1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        titleId = R.string.activity_cycling,
        color = ActivityTypeColor.Color2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        titleId = R.string.activity_running,
        color = ActivityTypeColor.Color3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        titleId = R.string.activity_kung_fu,
        color = ActivityTypeColor.Color4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        titleId = R.string.activity_commute_home_to_university,
        color = ActivityTypeColor.Color1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        titleId = R.string.activity_commute_university_to_home,
        color = ActivityTypeColor.Color2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        titleId = R.string.travel,
        color = ActivityTypeColor.Color3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(
        ActivityCategory.Work,
        titleId = R.string.work,
        color = ActivityTypeColor.Color1,
        emoji = "💼"
    ),
    Gaming(
        ActivityCategory.Entertainment,
        titleId = R.string.gaming,
        color = ActivityTypeColor.Color1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        titleId = R.string.film,
        color = ActivityTypeColor.Color2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        titleId = R.string.activity_note,
        color = ActivityTypeColor.Color1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        titleId = R.string.health_note,
        color = ActivityTypeColor.Color2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        titleId = R.string.other,
        color = ActivityTypeColor.Color3,
        emoji = "🏷️"
    );
}