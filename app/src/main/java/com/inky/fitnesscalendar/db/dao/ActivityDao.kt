package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Vehicle
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ActivityDao {
    @Query("SELECT * FROM Activity ORDER BY start_time DESC")
    suspend fun loadActivities(): List<Activity>

    @Query("SELECT * FROM Activity ORDER BY start_time DESC")
    fun getActivities(): Flow<List<Activity>>

    @Query(
        "SELECT * FROM ACTIVITY WHERE " +
                "(type IN (:types) OR :isTypesEmpty) AND" +
                "(description LIKE :search OR type IN (:searchTypes) OR vehicle IN (:searchVehicles) OR :search IS NULL) AND" +
                "(end_time >= :start OR :start IS NULL) AND" +
                "(start_time <= :end OR :end IS NULL) " +
                "ORDER BY start_time DESC"
    )
    fun getFiltered(
        types: List<ActivityType>,
        isTypesEmpty: Boolean,
        search: String?,
        searchTypes: List<ActivityType>,
        searchVehicles: List<Vehicle>,
        start: Date?,
        end: Date?
    ): Flow<List<Activity>>

    @Query("SELECT * FROM ACTIVITY WHERE uid=(:id)")
    fun get(id: Int): Flow<Activity>

    @Upsert
    suspend fun save(activity: Activity)

    @Delete
    suspend fun delete(activity: Activity)

}