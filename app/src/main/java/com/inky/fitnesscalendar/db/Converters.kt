package com.inky.fitnesscalendar.db

import android.net.Uri
import androidx.room.TypeConverter
import com.inky.fitnesscalendar.data.EpochDay
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
    fun EpochDayToLong(day: EpochDay): Long {
        return day.day
    }

    @TypeConverter
    fun LongToEpochDay(value: Long): EpochDay {
        return EpochDay(value)
    }
}