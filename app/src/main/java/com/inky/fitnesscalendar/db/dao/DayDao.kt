package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.db.entities.Day
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query("SELECT image_name FROM Day WHERE image_name IS NOT NULL")
    suspend fun getImages(): List<ImageName>

    @Query("SELECT * FROM Day")
    fun getDays(): Flow<List<Day>>

    @Query("SELECT * FROM Day WHERE day = :day")
    fun get(day: EpochDay): Flow<Day?>

    @Upsert
    suspend fun upsert(day: Day)
}