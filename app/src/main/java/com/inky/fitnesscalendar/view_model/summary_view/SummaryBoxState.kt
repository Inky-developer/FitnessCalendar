package com.inky.fitnesscalendar.view_model.summary_view

import android.content.Context
import com.inky.fitnesscalendar.data.ActivityStatistics
import com.inky.fitnesscalendar.data.measure.Measure
import com.inky.fitnesscalendar.data.measure.takeIfNotNothing

data class SummaryBoxState(
    val totalActivities: String,
    val totalTime: String?,
    val averageTime: String?,
    val maximumTime: String?,
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
        totalTime = statistics.totalTime().formatIfNotNothing(context),
        averageTime = statistics.averageTime()?.formatIfNotNothing(context),
        maximumTime = statistics.maximalTime()?.formatIfNotNothing(context),
        totalDistance = statistics.totalDistance().formatIfNotNothing(context),
        averageDistance = statistics.averageDistance()?.formatIfNotNothing(context),
        averageSpeed = statistics.averageSpeed()?.formatIfNotNothing(context),
        averageMovingSpeed = statistics.averageMovingSpeed()?.formatIfNotNothing(context),
        totalAscent = statistics.totalAscent().formatIfNotNothing(context),
        totalDescent = statistics.totalDescent().formatIfNotNothing(context),
        averageAscent = statistics.averageAscent()?.formatIfNotNothing(context),
        averageDescent = statistics.averageDescent()?.formatIfNotNothing(context),
        averageHeartRate = statistics.averageHeartRate()?.formatIfNotNothing(context),
        maximumHeartRate = statistics.maximalHeartRate()?.formatIfNotNothing(context),
        averageTemperature = statistics.averageTemperature()?.formatIfNotNothing(context),
    )
}

fun <T : Measure> T.formatIfNotNothing(context: Context) =
    takeIfNotNothing()?.formatWithContext(context)