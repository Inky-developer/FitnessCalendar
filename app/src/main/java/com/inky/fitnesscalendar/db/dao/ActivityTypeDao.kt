package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Query
import com.inky.fitnesscalendar.data.ActivityType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ActivityTypeDao {
    @Query("SELECT * FROM ActivityType")
    abstract suspend fun loadTypes(): List<ActivityType>

    @Query("SELECT * FROM ActivityType")
    abstract fun getTypes(): Flow<List<ActivityType>>

    @Query("SELECT * FROM ActivityType WHERE uid in (:filterIds)")
    abstract fun getTypes(filterIds: List<Int>): Flow<List<ActivityType>>
}