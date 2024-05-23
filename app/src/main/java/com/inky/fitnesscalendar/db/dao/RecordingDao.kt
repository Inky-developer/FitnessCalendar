package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.inky.fitnesscalendar.data.Recording
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Query("SELECT * FROM recording ORDER BY start_time")
    fun getRecordings(): Flow<List<Recording>>

    @Insert
    suspend fun insert(recording: Recording)

    @Delete
    suspend fun delete(recording: Recording)
}