package com.inky.fitnesscalendar.db

import androidx.annotation.ColorRes
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityTypeColor

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE Activity SET type = \"UniversityCommute\" WHERE type = \"WorkCommute\"")
    }
}

@RenameColumn(tableName = "Activity", fromColumnName = "type", toColumnName = "type_id")
class Migration4To5Spec : AutoMigrationSpec

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE ActivityType (uid INTEGER PRIMARY KEY, activity_category TEXT NOT NULL, name TEXT NOT NULL, emoji TEXT NOT NULL, color_id INTEGER NOT NULL, has_vehicle INTEGER NOT NULL, has_duration INTEGER NOT NULL)")

        db.execSQL("CREATE TABLE TempActivity (uid INTEGER, type_id INTEGER, legacy_type_id TEXT, vehicle TEXT, description TEXT, start_time INTEGER, end_time INTEGER, feel TEXT, image_uri TEXT)")
        db.execSQL("INSERT INTO TempActivity(uid, legacy_type_id, vehicle, description, start_time, end_time, feel, image_uri) SELECT uid, type_id, vehicle, description, start_time, end_time, feel, image_uri FROM Activity")
        db.execSQL("CREATE TABLE TempRecording (uid INTEGER PRIMARY KEY, type_id INTEGER NOT NULL, legacy_type TEXT, vehicle TEXT, start_time INTEGER NOT NULL)")
        db.execSQL("INSERT INTO TempRecording(uid, legacy_type, vehicle, start_time) SELECT uid, type, vehicle, start_time FROM Recording")

        for ((index, type) in LegacyActivityType.entries.withIndex()) {
            db.execSQL(
                "INSERT INTO ActivityType (uid, activity_category, name, emoji, color_id, has_vehicle, has_duration)" +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf(
                    index + 1,
                    type.activityCategory,
                    type.title,
                    type.emoji,
                    type.colorId,
                    type.hasVehicle,
                    type.hasDuration
                )
            )

            db.execSQL(
                "UPDATE TempActivity SET type_id = ? WHERE legacy_type_id = ?",
                arrayOf(index + 1, type.toString())
            )
            db.execSQL(
                "UPDATE TempRecording SET type_id = ? WHERE legacy_type = ?",
                arrayOf(index + 1, type.toString())
            )
        }

        db.execSQL("DROP TABLE Activity")
        db.execSQL("CREATE TABLE Activity (uid INTEGER PRIMARY KEY, type_id INTEGER NOT NULL, vehicle TEXT, description TEXT NOT NULL, start_time INTEGER NOT NULL, end_time int NOT NULL, feel TEXT, image_uri TEXT, FOREIGN KEY (type_id) REFERENCES ActivityType (uid) ON DELETE RESTRICT)")
        db.execSQL("CREATE INDEX `index_Activity_start_time` ON Activity (`start_time`)")
        db.execSQL("INSERT INTO Activity SELECT uid, type_id, vehicle, description, start_time, end_time, feel, image_uri FROM TempActivity")
        db.execSQL("DROP TABLE TempActivity")
        db.execSQL("DROP TABLE RECORDING")
        db.execSQL("CREATE TABLE Recording (uid INTEGER PRIMARY KEY, type_id INTEGER NOT NULL, vehicle TEXT, start_time INTEGER NOT NULL, FOREIGN KEY (type_id) REFERENCES ActivityType (uid) ON DELETE RESTRICT)")
        db.execSQL("INSERT INTO Recording SELECT uid, type_id, vehicle, start_time FROM TempRecording")
        db.execSQL("DROP TABLE TempRecording")
    }
}

enum class LegacyActivityType(
    val activityCategory: ActivityCategory,
    val title: String,
    val emoji: String,
    @ColorRes val colorId: Int,
    val hasVehicle: Boolean = false,
    val hasDuration: Boolean = true,
) {
    Bouldering(
        ActivityCategory.Sports,
        title = "Bouldering",
        colorId = R.color.stats_1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        title = "Cycling",
        colorId = R.color.stats_2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        title = "Running",
        colorId = R.color.stats_3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        title = "Kung Fu",
        colorId = R.color.stats_4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        title = "Home → Uni",
        colorId = R.color.stats_1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        title = "Uni → Home",
        colorId = R.color.stats_2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        title = "Travel",
        colorId = R.color.stats_3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, title = "Work", colorId = R.color.stats_1, emoji = "💼"),
    Gaming(
        ActivityCategory.Entertainment,
        title = "Gaming",
        colorId = R.color.stats_1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        title = "Film",
        colorId = R.color.stats_2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        title = "Note",
        colorId = R.color.stats_1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        title = "Health Note",
        colorId = R.color.stats_2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        title = "Other",
        colorId = R.color.stats_3,
        emoji = "🏷️"
    );
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE TempActivityType (uid INTEGER PRIMARY KEY, activity_category TEXT NOT NULL, name TEXT NOT NULL, emoji TEXT NOT NULL, color TEXT, color_id INTEGER NOT NULL, has_vehicle INTEGER NOT NULL, has_duration INTEGER NOT NULL)")
        db.execSQL("INSERT INTO TempActivityType(uid, activity_category, name, emoji, color_id, has_vehicle, has_duration) SELECT uid, activity_category, name, emoji, color_id, has_vehicle, has_duration FROM ActivityType")

        for (color in ActivityTypeColor.entries) {
            db.execSQL(
                "UPDATE TempActivityType SET color = ? WHERE color_id = ?",
                arrayOf(color.toString(), color.colorId)
            )
        }
        db.execSQL(
            "UPDATE TempActivityType SET color = ? WHERE color_id = NULL",
            arrayOf(ActivityTypeColor.ColorOther)
        )

        db.execSQL("DROP TABLE ActivityType")
        db.execSQL("CREATE TABLE ActivityType (uid INTEGER PRIMARY KEY, activity_category TEXT NOT NULL, name TEXT NOT NULL, emoji TEXT NOT NULL, color TEXT NOT NULL, has_vehicle INTEGER NOT NULL, has_duration INTEGER NOT NULL)")
        db.execSQL("INSERT INTO ActivityType(uid, activity_category, name, emoji, color, has_vehicle, has_duration) SELECT uid, activity_category, name, emoji, color, has_vehicle, has_duration FROM TempActivityType")
        db.execSQL("DROP TABLE TempActivityType")
    }
}

val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS FilterHistoryItem (`uid` INTEGER, `type` TEXT NOT NULL, `text` TEXT, `date_range_option` TEXT, `category` TEXT, `type_id` INTEGER, `attribute` TEXT, `attribute_state` INTEGER, `last_updated` INTEGER NOT NULL, PRIMARY KEY(`uid`), FOREIGN KEY(`type_id`) REFERENCES `ActivityType`(`uid`) ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_FilterHistoryItem_last_updated` ON FilterHistoryItem (`last_updated`)")
    }
}

val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Activity_type_id` ON Activity (`type_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_FilterHistoryItem_type_id` ON FilterHistoryItem (`type_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Recording_type_id` ON Recording (`type_id`)")
    }
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Day` (`day` INTEGER NOT NULL, `description` TEXT NOT NULL, `feel` TEXT, `image_uri` TEXT, PRIMARY KEY(`day`))")
    }
}