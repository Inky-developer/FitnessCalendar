package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.data.TypeRecording
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {
    @Transaction
    @Query("SELECT * FROM recording ORDER BY start_time")
    fun getRecordings(): Flow<List<TypeRecording>>

    @Query("SELECT * FROM recording WHERE type_id=:uid")
    suspend fun getById(uid: Int): Recording?

    @Insert
    suspend fun insert(recording: Recording)

    @Delete
    suspend fun delete(recording: Recording)
}