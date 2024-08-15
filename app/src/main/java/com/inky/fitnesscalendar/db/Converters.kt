package com.inky.fitnesscalendar.db

import android.net.Uri
import androidx.room.TypeConverter
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Intensity
import com.inky.fitnesscalendar.data.gpx.GpxTrackPoint
import com.inky.fitnesscalendar.data.measure.Distance
import kotlinx.serialization.encodeToString
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
    fun uriToString(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun uriFromString(value: String?): Uri? {
        return value?.let { Uri.parse(it) }
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
    fun distanceToLong(distance: Distance): Long {
        return distance.meters
    }

    @TypeConverter
    fun longToDistance(value: Long): Distance {
        return Distance(meters = value)
    }

    @TypeConverter
    fun intensityToLong(intensity: Intensity): Byte {
        return intensity.value
    }

    @TypeConverter
    fun byteToIntensity(value: Byte): Intensity {
        return Intensity(value)
    }

    @TypeConverter
    fun trackPointsToByteArray(points: List<GpxTrackPoint>): ByteArray {
        return Json.encodeToString(points).toByteArray()
    }

    @TypeConverter
    fun byteArrayToTrackPoints(array: ByteArray): List<GpxTrackPoint> {
        return Json.decodeFromString(array.decodeToString())
    }
}