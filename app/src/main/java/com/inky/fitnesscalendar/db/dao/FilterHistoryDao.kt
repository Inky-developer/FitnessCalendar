package com.inky.fitnesscalendar.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.inky.fitnesscalendar.db.entities.FilterHistoryItem
import com.inky.fitnesscalendar.db.entities.FullFilterHistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FilterHistoryDao {
    @Transaction
    @Query("SELECT * FROM FilterHistoryItem ORDER BY last_updated DESC")
    abstract fun getItems(): Flow<List<FullFilterHistoryItem>>

    @Query("DELETE FROM FilterHistoryItem WHERE uid NOT IN (SELECT uid FROM FilterHistoryItem ORDER BY last_updated DESC LIMIT :count)")
    abstract fun onlyKeepNewest(count: Int)

    @Upsert
    abstract suspend fun upsert(item: FilterHistoryItem)
}