package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.db.entities.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT Place.*, COUNT(Activity.place_id) as count, MAX(Activity.start_time) AS last_used FROM Place LEFT JOIN Activity ON Place.uid = Activity.place_id GROUP BY place.uid ORDER BY count DESC, last_used DESC  ")
    fun getAll(): Flow<List<Place>>

    @Query("SELECT place_id, COUNT(*) AS count FROM Activity WHERE place_id IS NOT NULL GROUP BY place_id")
    fun getWithActivityCount():
            Flow<Map<@MapColumn(columnName = "place_id") Int, @MapColumn(columnName = "count") Int>>

    @Query("SELECT * FROM place WHERE uid = :id")
    fun get(id: Int): Flow<Place>

    @Query("SELECT image_name FROM Place WHERE image_name IS NOT NULL")
    suspend fun getImages(): List<ImageName>

    @Upsert
    suspend fun upsert(place: Place)

    @Delete
    suspend fun delete(place: Place)
}