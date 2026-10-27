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
import com.inky.fitnesscalendar.db.entities.RichDay
import com.inky.fitnesscalendar.db.entities.Steps
import kotlinx.coroutines.flow.Flow

@Dao
interface DayDao {
    @Query("SELECT image_name FROM Day WHERE image_name IS NOT NULL")
    suspend fun getImages(): List<ImageName>

    @Transaction
    @Query("SELECT * FROM AllDaysView ORDER BY epoch_day DESC")
    fun getDays(): Flow<Map<@MapColumn(columnName = "epoch_day") EpochDay, RichDay>>

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

    @Transaction
    @Query("SELECT * FROM AllDaysView WHERE epoch_day = :day")
    fun get(day: EpochDay): Flow<RichDay?>

    @Transaction
    suspend fun upsertOrDelete(day: Day) {
        if (day.isDefault()) {
            internalDelete(day)
        } else {
            internalUpsert(day)
        }
    }

    @Query("SELECT * FROM Steps where day = :day AND frame = :frame")
    suspend fun stepsAt(day: EpochDay, frame: Int): Steps?

    @Transaction
    suspend fun recordSteps(steps: Steps) {
        val previousSteps = stepsAt(steps.day, steps.frame)?.steps ?: 0
        val newSteps = Steps(steps.day, steps.frame, steps = previousSteps + steps.steps)
        internalStepsUpsert(newSteps)
    }

    @Upsert
    suspend fun internalStepsUpsert(steps: Steps)

    @Upsert
    suspend fun internalUpsert(day: Day)

    @Delete
    suspend fun internalDelete(day: Day)
}