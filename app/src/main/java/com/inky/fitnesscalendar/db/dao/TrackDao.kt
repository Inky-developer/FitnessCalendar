package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.db.entities.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM TRACK")
    suspend fun loadAll(): List<Track>

    @Query("SELECT * FROM Track WHERE uid = :id")
    suspend fun load(id: Int): Track?

    @Query("SELECT * FROM Track WHERE activity_id = :activityId")
    fun getByActivityId(activityId: Int): Flow<Track?>

    @Upsert
    suspend fun upsert(track: Track)
}