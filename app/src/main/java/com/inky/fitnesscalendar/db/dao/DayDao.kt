package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.db.entities.Day
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query("SELECT image_name FROM Day WHERE image_name IS NOT NULL")
    suspend fun getImages(): List<ImageName>

    @Query("SELECT * FROM Day ORDER BY day DESC")
    fun getDays(): Flow<Map<@MapColumn(columnName = "day") EpochDay, Day>>

    @Query(
        "SELECT day FROM Day WHERE " +
                "(description LIKE :search OR :search IS NULL) AND" +
                "(feel IN (:feels) OR :isFeelEmpty) AND" +
                "(day >= :start OR :start IS NULL) AND" +
                "(day <= :end OR :end IS NULL) AND" +
                "(image_name IS NOT NULL == :hasImage OR :hasImage IS NULL)"
    )
    fun getDaysFiltered(
        search: String?,
        feels: List<Feel>,
        isFeelEmpty: Boolean,
        start: EpochDay?,
        end: EpochDay?,
        hasImage: Boolean?
    ): Flow<List<EpochDay>>

    @Query("SELECT * FROM Day WHERE day = :day")
    fun get(day: EpochDay): Flow<Day?>

    @Transaction
    suspend fun upsertOrDelete(day: Day) {
        if (day.isDefault()) {
            internalDelete(day)
        } else {
            internalUpsert(day)
        }
    }

    @Upsert
    suspend fun internalUpsert(day: Day)

    @Delete
    suspend fun internalDelete(day: Day)
}