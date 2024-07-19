package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Transaction
    @Query("SELECT * FROM recording ORDER BY start_time")
    fun getRecordings(): Flow<List<RichRecording>>

    @Query("SELECT * FROM recording WHERE uid=:uid")
    suspend fun getById(uid: Int): Recording?

    @Insert
    suspend fun insert(recording: Recording): Long

    @Delete
    suspend fun delete(recording: Recording)
}