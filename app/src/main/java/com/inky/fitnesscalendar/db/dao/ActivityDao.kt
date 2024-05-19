package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM Activity ORDER BY start_time DESC")
    fun getAll(): Flow<List<Activity>>

    @Query("SELECT * FROM ACTIVITY WHERE uid=(:id)")
    fun get(id: Int): Flow<Activity>

    @Upsert
    suspend fun save(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

}