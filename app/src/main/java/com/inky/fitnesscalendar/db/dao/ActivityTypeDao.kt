package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.db.entities.ActivityType
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ActivityTypeDao {
    @Query("SELECT * FROM ActivityType ORDER BY name")
    abstract suspend fun loadTypes(): List<ActivityType>

    @Query("SELECT * FROM ACTIVITYTYPE WHERE uid = :id")
    abstract suspend fun get(id: Int): ActivityType?

    @Query("SELECT * FROM ActivityType ORDER BY name")
    abstract fun getTypes(): Flow<List<ActivityType>>

    @Query("SELECT * FROM ActivityType WHERE uid in (:filterIds) ORDER BY name")
    abstract fun getTypes(filterIds: List<Int>): Flow<List<ActivityType>>

    @Query("SELECT * FROM ActivityType ORDER BY name")
    abstract fun getActivityTypesByCategory(): Flow<Map<@MapColumn(columnName = "activity_category") ActivityCategory, List<ActivityType>>>

    @Upsert
    abstract suspend fun save(type: ActivityType)

    @Delete
    abstract suspend fun delete(type: ActivityType)
}