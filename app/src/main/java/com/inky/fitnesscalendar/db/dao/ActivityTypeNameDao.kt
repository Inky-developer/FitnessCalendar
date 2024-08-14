package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.inky.fitnesscalendar.db.entities.ActivityTypeName
import com.inky.fitnesscalendar.db.entities.RichActivityTypeName
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ActivityTypeNameDao {
    @Transaction
    @Query("SELECT * FROM ActivityTypeName")
    abstract fun getAll(): Flow<List<RichActivityTypeName>>

    @Upsert
    abstract suspend fun set(typeName: ActivityTypeName)
}