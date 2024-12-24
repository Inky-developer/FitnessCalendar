package com.inky.fitnesscalendar.view_model.summary_view

import android.content.Context
import com.inky.fitnesscalendar.data.ActivityStatistics

data class SummaryBoxState(
    val totalActivities: String,
    val totalTime: String?,
    val averageTime: String?,
    val totalDistance: String?,
    val averageDistance: String?,
    val averageSpeed: String?,
    val averageMovingSpeed: String?,
//    val maximumSpeed: String?,
//    val minimumElevation: String?,
//    val maximumElevation: String?,
    val totalAscent: String?,
    val totalDescent: String?,
    val averageAscent: String?,
    val averageDescent: String?,
    val averageHeartRate: String?,
    val maximumHeartRate: String?,
//    val minimumTemperature: String?,
//    val maximumTemperature: String?,
    val averageTemperature: String?,
) {
    constructor(context: Context, statistics: ActivityStatistics) : this(
        totalActivities = statistics.size.toString(),
        totalTime = statistics.totalTime().takeIf { it.elapsedMs > 0L }?.format(),
        averageTime = statistics.averageTime()?.format(),
        totalDistance = statistics.totalDistance().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        averageDistance = statistics.averageDistance()?.formatWithContext(context),
        averageSpeed = statistics.averageSpeed()?.formatWithContext(context),
        averageMovingSpeed = statistics.averageMovingSpeed()?.formatWithContext(context),
        totalAscent = statistics.totalAscent().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        totalDescent = statistics.totalDescent().takeIf { it.meters > 0 }
            ?.formatWithContext(context),
        averageAscent = statistics.averageAscent()?.formatWithContext(context),
        averageDescent = statistics.averageDescent()?.formatWithContext(context),
        averageHeartRate = statistics.averageHeartRate()?.formatWithContext(context),
        maximumHeartRate = statistics.maximalHeartRate()?.formatWithContext(context),
        averageTemperature = statistics.averageTemperature()?.formatWithContext(context),
    )
}