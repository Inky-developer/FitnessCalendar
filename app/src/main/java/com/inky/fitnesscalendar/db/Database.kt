package com.inky.fitnesscalendar.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Recording
import com.inky.fitnesscalendar.data.activity_filter.FilterHistoryItem
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.FilterHistoryDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.util.DATABASE_NAME

@Database(
    version = 8,
    entities = [Activity::class, Recording::class, ActivityType::class, FilterHistoryItem::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = Migration4To5Spec::class),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun recordingDao(): RecordingDao

    abstract fun activityTypeDao(): ActivityTypeDao

    abstract fun filterHistoryDao(): FilterHistoryDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) =
            Room
                .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8)
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        loadDefaultData(db)
                    }
                })
                .build()
    }
}