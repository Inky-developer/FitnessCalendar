package com.inky.fitnesscalendar.db

import androidx.room.TypeConverter
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Distance
import com.inky.fitnesscalendar.data.measure.Duration
import com.inky.fitnesscalendar.data.measure.meters
import com.inky.fitnesscalendar.data.measure.ms
import com.inky.fitnesscalendar.db.entities.Activity
import kotlinx.serialization.json.Json
import java.util.Date

class Converters {
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun dateFromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun epochDayToLong(day: EpochDay): Long {
        return day.day
    }

    @TypeConverter
    fun longToEpochDay(value: Long): EpochDay {
        return EpochDay(value)
    }

    @TypeConverter
    fun distanceToDouble(distance: Distance): Double {
        return distance.meters
    }

    @TypeConverter
    fun doubleToDistance(value: Double): Distance {
        return value.meters()
    }

    @TypeConverter
    fun intensityToLong(intensity: Intensity): Byte {
        return intensity.value
    }

    @TypeConverter
    fun byteToIntensity(value: Byte): Intensity {
        return Intensity(value)
    }

    // TODO: This is very wasteful and should use a more efficient format
    // (Or ideally sqlite's inbuilt json feature)
    @TypeConverter
    fun trackPointsToByteArray(points: List<GpxTrackPoint>): ByteArray {
        return Json.encodeToString(points).toByteArray()
    }

    @TypeConverter
    fun byteArrayToTrackPoints(array: ByteArray): List<GpxTrackPoint> {
        return Json.decodeFromString(array.decodeToString())
    }

    @TypeConverter
    fun serializedTrackPreviewToString(preview: Activity.SerializedTrackPreview): String {
        return preview.value
    }

    @TypeConverter
    fun stringToSerializedTrackPreview(value: String): Activity.SerializedTrackPreview {
        return Activity.SerializedTrackPreview.assumeValid(value)
    }

    @TypeConverter
    fun imageNameToString(value: ImageName) = value.name

    @TypeConverter
    fun stringToImageName(value: String) = ImageName(value)

    @TypeConverter
    fun durationToLong(duration: Duration) = duration.elapsedMs

    @TypeConverter
    fun longToDuration(value: Long) = value.ms()
}