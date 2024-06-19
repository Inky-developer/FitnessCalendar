package com.inky.fitnesscalendar.db

import androidx.annotation.ColorRes
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory

// TODO: Add test for this
fun loadDefaultData(db: SupportSQLiteDatabase) {
    db.beginTransaction()
    try {
        for (type in DefaultActivityType.entries) {
            db.execSQL(
                "INSERT INTO ActivityType(activity_category, name, emoji, color_id, has_vehicle, has_duration) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(
                    type.activityCategory,
                    type.title,
                    type.emoji,
                    type.colorId,
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
    val title: String,
    val emoji: String,
    @ColorRes val colorId: Int,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(
        ActivityCategory.Sports,
        title = "Bouldering",
        colorId = R.color.stats_1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        title = "Cycling",
        colorId = R.color.stats_2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        title = "Running",
        colorId = R.color.stats_3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        title = "Kung Fu",
        colorId = R.color.stats_4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        title = "Home → Uni",
        colorId = R.color.stats_1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        title = "Uni → Home",
        colorId = R.color.stats_2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        title = "Travel",
        colorId = R.color.stats_3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, title = "Work", colorId = R.color.stats_1, emoji = "💼"),
    Gaming(
        ActivityCategory.Entertainment,
        title = "Gaming",
        colorId = R.color.stats_1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        title = "Film",
        colorId = R.color.stats_2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        title = "Note",
        colorId = R.color.stats_1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        title = "Health Note",
        colorId = R.color.stats_2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        title = "Other",
        colorId = R.color.stats_3,
        emoji = "🏷️"
    );
}