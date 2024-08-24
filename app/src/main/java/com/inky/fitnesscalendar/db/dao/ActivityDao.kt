package com.inky.fitnesscalendar.db.dao

import android.net.Uri
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichActivity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
abstract class ActivityDao {
    @Transaction
    @Query("SELECT * FROM Activity ORDER BY start_time DESC")
    abstract suspend fun loadActivities(): List<RichActivity>

    @Transaction
    @Query("SELECT * FROM Activity ORDER BY start_time DESC LIMIT :n")
    abstract suspend fun loadMostRecentActivities(n: Int): List<RichActivity>

    @Transaction
    @Query("SELECT * FROM Activity ORDER BY start_time DESC LIMIT 1")
    abstract fun getMostRecentActivity(): Flow<RichActivity?>

    @Transaction
    @Query("SELECT * FROM Activity ORDER BY start_time DESC")
    abstract fun getActivities(): Flow<List<RichActivity>>

    @Transaction
    @Query(
        "SELECT Activity.* FROM Activity " +
                "INNER JOIN ActivityType AS type ON Activity.type_id = type.uid " +
                "LEFT JOIN Place AS place ON Activity.place_id = place.uid " +
                "WHERE " +
                "   (type_id IN (:typeIds) OR :isTypesEmpty) AND" +
                "   (type.activity_category in (:categories) or :isCategoriesEmpty) AND" +
                "   (place_id in (:placeIds) OR :isPlacesEmpty) AND" +
                "   (vehicle in (:vehicles) OR :isVehiclesEmpty) AND" +
                "   (feel in (:feels) OR :isFeelsEmpty) AND" +
                "   (description LIKE :search OR type.name LIKE :search OR place.name LIKE :search OR vehicle IN (:searchVehicles) OR :search IS NULL) AND" +
                "   (end_time >= :start OR :start IS NULL) AND" +
                "   (start_time <= :end OR :end IS NULL) AND" +
                "   ((description != '') == :hasDescription OR :hasDescription IS NULL) AND" +
                "   ((feel IS NOT NULL) == :hasFeel OR :hasFeel IS NULL) AND" +
                "   ((image_uri IS NOT NULL) == :hasImage OR :hasImage IS NULL) AND" +
                "   ((place_id IS NOT NULL) == :hasPlace OR :hasPlace IS NULL) AND" +
                "   ((track_preview IS NOT NULL) == :hasTrack OR :hasTrack IS NULL)" +
                "ORDER BY " +
                "   CASE WHEN :order = 0 THEN start_time END ASC," +
                "   CASE WHEN :order = 1 THEN start_time END DESC"
    )
    abstract fun getFiltered(
        order: Int,
        typeIds: List<Int>,
        isTypesEmpty: Boolean,
        categories: List<String>,
        isCategoriesEmpty: Boolean,
        placeIds: List<Int>,
        isPlacesEmpty: Boolean,
        vehicles: List<Vehicle>,
        isVehiclesEmpty: Boolean,
        feels: List<Feel>,
        isFeelsEmpty: Boolean,
        search: String?,
        searchVehicles: List<Vehicle>,
        start: Date?,
        end: Date?,
        hasDescription: Boolean?,
        hasFeel: Boolean?,
        hasImage: Boolean?,
        hasPlace: Boolean?,
        hasTrack: Boolean?,
    ): Flow<List<RichActivity>>

    @Transaction
    @Query("SELECT * FROM ACTIVITY WHERE uid=(:id)")
    abstract fun get(id: Int): Flow<RichActivity>

    @Query("SELECT image_uri FROM ACTIVITY WHERE image_uri IS NOT NULL")
    abstract suspend fun getImages(): List<Uri>

    @Upsert
    abstract suspend fun save(activity: Activity): Long

    @Delete
    abstract suspend fun delete(activity: Activity)

    @Delete
    abstract suspend fun deleteRecording(recording: Recording)

    @Transaction
    open suspend fun stopRecording(recording: Recording, activity: Activity) {
        deleteRecording(recording)
        save(activity)
    }

}