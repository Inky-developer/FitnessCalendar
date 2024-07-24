package com.inky.fitnesscalendar.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.DayDao
import com.inky.fitnesscalendar.db.dao.FilterHistoryDao
import com.inky.fitnesscalendar.db.dao.PlaceDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.FilterHistoryItem
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.util.DATABASE_NAME

@Database(
    version = 17,
    entities = [Activity::class, Recording::class, ActivityType::class, FilterHistoryItem::class, Day::class, Place::class],
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5, spec = Migration4To5Spec::class),
        AutoMigration(from = 10, to = 11),
        AutoMigration(from = 11, to = 12),
        AutoMigration(from = 13, to = 14),
        AutoMigration(from = 14, to = 15),
        AutoMigration(from = 15, to = 16),
        AutoMigration(from = 16, to = 17),
    ]
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    abstract fun recordingDao(): RecordingDao

    abstract fun activityTypeDao(): ActivityTypeDao

    abstract fun filterHistoryDao(): FilterHistoryDao

    abstract fun dayDao(): DayDao

    abstract fun placeDao(): PlaceDao

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
                .addMigrations(
                    MIGRATION_1_2,
                    MIGRATION_5_6,
                    MIGRATION_6_7,
                    MIGRATION_7_8,
                    MIGRATION_8_9,
                    MIGRATION_9_10,
                    MIGRATION_12_13
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        loadDefaultData(db, context)
                    }
                })
                .build()
    }
}