package com.inky.fitnesscalendar.db

import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityTypeColor

// TODO: Add test for this
fun loadDefaultData(db: SupportSQLiteDatabase) {
    db.beginTransaction()
    try {
        for (type in DefaultActivityType.entries) {
            db.execSQL(
                "INSERT INTO ActivityType(activity_category, name, emoji, color, has_vehicle, has_duration) VALUES (?, ?, ?, ?, ?, ?)",
                arrayOf(
                    type.activityCategory,
                    type.title,
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
    val title: String,
    val emoji: String,
    val color: ActivityTypeColor,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(
        ActivityCategory.Sports,
        title = "Bouldering",
        color = ActivityTypeColor.Color1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        title = "Cycling",
        color = ActivityTypeColor.Color2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        title = "Running",
        color = ActivityTypeColor.Color3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        title = "Kung Fu",
        color = ActivityTypeColor.Color4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        title = "Home → Uni",
        color = ActivityTypeColor.Color1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        title = "Uni → Home",
        color = ActivityTypeColor.Color2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        title = "Travel",
        color = ActivityTypeColor.Color3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, title = "Work", color = ActivityTypeColor.Color1, emoji = "💼"),
    Gaming(
        ActivityCategory.Entertainment,
        title = "Gaming",
        color = ActivityTypeColor.Color1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        title = "Film",
        color = ActivityTypeColor.Color2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        title = "Note",
        color = ActivityTypeColor.Color1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        title = "Health Note",
        color = ActivityTypeColor.Color2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        title = "Other",
        color = ActivityTypeColor.Color3,
        emoji = "🏷️"
    );
}