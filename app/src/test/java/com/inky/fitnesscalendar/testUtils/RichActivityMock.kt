package com.inky.fitnesscalendar.testUtils

import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.data.measure.VerticalDistance
import com.inky.fitnesscalendar.data.measure.bpm
import com.inky.fitnesscalendar.data.measure.kilometers
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import java.time.Instant
import java.util.Date

fun mockSportsActivity(
    durationSeconds: Long = 4000,
    description: String = "",
    distance: Distance = 20.0.kilometers(),
    averageHeartRate: HeartFrequency = 125.0.bpm(),
    verticalDistance: VerticalDistance = VerticalDistance(meters = 1250.0)
): RichActivity {
    val activity = Activity(
        typeId = 0,
        startTime = Date.from(Instant.now()),
        endTime = Date.from(Instant.now().plusSeconds(durationSeconds)),
        distance = distance,
        averageHeartRate = averageHeartRate,
        temperature = Temperature(celsius = 20.0),
        totalAscent = verticalDistance,
        description = description
    )
    return RichActivity(
        activity = activity,
        type = ActivityType(
            uid = 0,
            activityCategory = ActivityCategory.Sports,
            name = "Biking",
            emoji = "🚴‍♂️",
            color = ContentColor.Color1,
        ),
        place = null,
        images = emptyList()
    )
}