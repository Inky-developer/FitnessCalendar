package com.inky.fitnesscalendar.db

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.data.measure.meters
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.util.toDate
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.random.Random

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

/**
 * Produces some default activities for testing purposes
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
fun generateSampleActivities(db: AppDatabase, seed: Int = 42) = runBlocking {
    val startDay = LocalDate.of(2025, 1, 1)
    val endDay = LocalDate.of(2025, 6, 1)

    val random = Random(seed)

    val activityTypes = db.activityTypeDao().loadTypes()

    val sports = activityTypes.filter { it.name == "Cycling" || it.name == "Running" }

    for (day in startDay.datesUntil(endDay, Period.ofDays(2))) {
        val startTime = day.atTime(random.nextInt(19), random.nextInt(60))
        val endTime = startTime.plusMinutes(random.nextLong(60 * 4))
        val activityType = sports.random(random)
        val activity = Activity(
            typeId = activityType.uid!!,
            startTime = startTime.toDate(ZoneId.of("UTC")),
            endTime = endTime.toDate(ZoneId.of("UTC")),
            description = "Some ${activityType.name}",
            favorite = random.nextDouble() < 0.05,
            distance = random.nextDouble(8000.0, 80000.0).meters(),
            temperature = Temperature(celsius = random.nextDouble(-10.0, 40.0)),
            averageHeartRate = HeartFrequency(bpm = random.nextDouble(120.0, 170.0)),
            maximalHeartRate = HeartFrequency(bpm = random.nextDouble(150.0, 200.0)),
        )
        db.activityDao().save(activity)
    }
}