package com.inky.fitnesscalendar.db

import android.content.Context
import android.util.Log
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeNameDao
import com.inky.fitnesscalendar.db.dao.DayDao
import com.inky.fitnesscalendar.db.dao.FilterHistoryDao
import com.inky.fitnesscalendar.db.dao.PlaceDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.db.dao.TrackDao
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.ActivityTypeName
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.FilterHistoryItem
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.util.DATABASE_NAME

private const val TAG = "Database"

@Database(
    version = 34,
    entities = [
        Activity::class,
        Recording::class,
        ActivityType::class,
        FilterHistoryItem::class,
        Day::class,
        Place::class,
        ActivityTypeName::class,
        Track::class,
    ],
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
        AutoMigration(from = 18, to = 19),
        AutoMigration(from = 19, to = 20),
        AutoMigration(from = 20, to = 21),
        AutoMigration(from = 21, to = 22),
        AutoMigration(from = 22, to = 23),
        AutoMigration(from = 23, to = 24, spec = Migration23To24Spec::class),
        AutoMigration(from = 25, to = 26),
        AutoMigration(from = 28, to = 29),
        AutoMigration(from = 29, to = 30),
        AutoMigration(from = 30, to = 31),
        AutoMigration(from = 31, to = 32),
        AutoMigration(from = 32, to = 33),
        AutoMigration(from = 33, to = 34),
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

    abstract fun activityTypeNameDao(): ActivityTypeNameDao

    abstract fun trackDao(): TrackDao

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
                    MIGRATION_12_13,
                    MIGRATION_17_18,
                    MIGRATION_24_25,
                    MIGRATION_26_27,
                    MIGRATION_27_28,
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        loadDefaultData(db, context)
                    }

                    override fun onOpen(db: SupportSQLiteDatabase) {
                        Log.i(TAG, "DB version: ${db.version}")
                    }
                })
                .build()
    }
}