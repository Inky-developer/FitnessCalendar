package com.inky.fitnesscalendar.db.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.db.entities.Day
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query("SELECT image_uri FROM Day WHERE image_uri IS NOT NULL")
    suspend fun getImages(): List<Uri>

    @Query("SELECT * FROM Day")
    fun getDays(): Flow<Map<@MapColumn(columnName = "day") EpochDay, Day>>

    @Query("SELECT * FROM Day WHERE day = :day")
    fun get(day: EpochDay): Flow<Day?>

    @Upsert
    suspend fun upsert(day: Day)
}