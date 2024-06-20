package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.ActivityCategory
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

    @Query("SELECT * FROM ActivityType")
    abstract fun getActivityTypesByCategory(): Flow<Map<@MapColumn(columnName = "activity_category") ActivityCategory, List<ActivityType>>>

    @Upsert
    abstract suspend fun save(type: ActivityType)
}