package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.inky.fitnesscalendar.db.entities.Place
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaceDao {
    @Query("SELECT * FROM place ORDER BY name")
    fun getAll(): Flow<List<Place>>

    @Query("SELECT place_id, COUNT(*) AS count FROM Activity WHERE place_id IS NOT NULL GROUP BY place_id")
    fun getWithActivityCount():
            Flow<Map<@MapColumn(columnName = "place_id") Int, @MapColumn(columnName = "count") Int>>

    @Query("SELECT * FROM place WHERE uid = :id")
    fun get(id: Int): Flow<Place>

    @Upsert
    suspend fun upsert(place: Place)

    @Delete
    suspend fun delete(place: Place)
}