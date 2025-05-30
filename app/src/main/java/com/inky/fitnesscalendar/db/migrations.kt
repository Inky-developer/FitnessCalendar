package com.inky.fitnesscalendar.db

import androidx.annotation.ColorRes
import androidx.room.DeleteColumn
import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor

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
                arrayOf<Any>(
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
                arrayOf<Any>(index + 1, type.toString())
            )
            db.execSQL(
                "UPDATE TempRecording SET type_id = ? WHERE legacy_type = ?",
                arrayOf<Any>(index + 1, type.toString())
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
        colorId = R.color.content_1,
        emoji = "🧗"
    ),
    Cycling(
        ActivityCategory.Sports,
        title = "Cycling",
        colorId = R.color.content_2,
        emoji = "🚴"
    ),
    Running(
        ActivityCategory.Sports,
        title = "Running",
        colorId = R.color.content_3,
        emoji = "🏃"
    ),
    KungFu(
        ActivityCategory.Sports,
        title = "Kung Fu",
        colorId = R.color.content_4,
        emoji = "🥋"
    ),
    UniversityCommute(
        ActivityCategory.Travel,
        title = "Home → Uni",
        colorId = R.color.content_1,
        emoji = "🏢",
        hasVehicle = true
    ),
    HomeCommute(
        ActivityCategory.Travel,
        title = "Uni → Home",
        colorId = R.color.content_2,
        emoji = "🏡",
        hasVehicle = true
    ),
    Travel(
        ActivityCategory.Travel,
        title = "Travel",
        colorId = R.color.content_3,
        emoji = "🗺️",
        hasVehicle = true
    ),
    Work(ActivityCategory.Work, title = "Work", colorId = R.color.content_1, emoji = "💼"),
    Gaming(
        ActivityCategory.Entertainment,
        title = "Gaming",
        colorId = R.color.content_1,
        emoji = "🎮"
    ),
    Film(
        ActivityCategory.Entertainment,
        title = "Film",
        colorId = R.color.content_2,
        emoji = "🎬"
    ),
    Note(
        ActivityCategory.Other,
        title = "Note",
        colorId = R.color.content_1,
        emoji = "📓",
        hasDuration = false
    ),
    HealthNote(
        ActivityCategory.Other,
        title = "Health Note",
        colorId = R.color.content_2,
        emoji = "🩹",
        hasDuration = false
    ),
    Other(
        ActivityCategory.Other,
        title = "Other",
        colorId = R.color.content_3,
        emoji = "🏷️"
    );
}

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE TempActivityType (uid INTEGER PRIMARY KEY, activity_category TEXT NOT NULL, name TEXT NOT NULL, emoji TEXT NOT NULL, color TEXT, color_id INTEGER NOT NULL, has_vehicle INTEGER NOT NULL, has_duration INTEGER NOT NULL)")
        db.execSQL("INSERT INTO TempActivityType(uid, activity_category, name, emoji, color_id, has_vehicle, has_duration) SELECT uid, activity_category, name, emoji, color_id, has_vehicle, has_duration FROM ActivityType")

        for (color in ContentColor.entries) {
            db.execSQL(
                "UPDATE TempActivityType SET color = ? WHERE color_id = ?",
                arrayOf<Any>(color.toString(), color.colorId)
            )
        }
        db.execSQL(
            "UPDATE TempActivityType SET color = ? WHERE color_id = NULL",
            arrayOf(ContentColor.ColorOther)
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

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `Place` (`uid` INTEGER, `name` TEXT NOT NULL, `color` TEXT NOT NULL, PRIMARY KEY(`uid`))")
        db.execSQL("ALTER TABLE `Activity` ADD COLUMN `place_id` INTEGER REFERENCES `Place`(`uid`) ON DELETE RESTRICT")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Activity_place_id` ON `Activity` (`place_id`)")
    }
}

val MIGRATION_17_18 = object : Migration(17, 18) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE FilterHistoryItem")
        db.execSQL("CREATE TABLE IF NOT EXISTS FilterHistoryItem (`uid` INTEGER, `type` TEXT NOT NULL, `text` TEXT, `date_range_start` INTEGER, `date_range_end` INTEGER, `date_range_name` TEXT, `category` TEXT, `type_id` INTEGER, `place_id` INTEGER, `attribute` TEXT, `attribute_state` INTEGER, `last_updated` INTEGER NOT NULL, PRIMARY KEY(`uid`), FOREIGN KEY(`type_id`) REFERENCES `ActivityType`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE , FOREIGN KEY(`place_id`) REFERENCES `Place`(`uid`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_FilterHistoryItem_last_updated` ON `FilterHistoryItem` (`last_updated`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_FilterHistoryItem_type_id` ON `FilterHistoryItem` (`type_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_FilterHistoryItem_place_id` ON `FilterHistoryItem` (`place_id`)")
    }
}

@RenameColumn(tableName = "Activity", fromColumnName = "image_uri", toColumnName = "image_name")
@RenameColumn(tableName = "Day", fromColumnName = "image_uri", toColumnName = "image_name")
class Migration23To24Spec : AutoMigrationSpec

val MIGRATION_24_25 = object : Migration(24, 25) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE Activity SET image_name = replace(image_name, rtrim(image_name, replace(image_name, '/', '')), '')")
        db.execSQL("UPDATE Day SET image_name = replace(image_name, rtrim(image_name, replace(image_name, '/', '')), '')")
    }
}

val MIGRATION_26_27 = object : Migration(26, 27) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `tempActivity` (`uid` INTEGER, `type_id` INTEGER NOT NULL, `place_id` INTEGER, `vehicle` TEXT, `description` TEXT NOT NULL, `favorite` INTEGER NOT NULL DEFAULT false, `image_name` TEXT, `feel` TEXT, `intensity` INTEGER, `start_time` INTEGER NOT NULL, `end_time` INTEGER NOT NULL, `distance` INTEGER, `wifi_bssid` TEXT, `track_preview` TEXT, PRIMARY KEY(`uid`), FOREIGN KEY(`type_id`) REFERENCES `ActivityType`(`uid`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`place_id`) REFERENCES `Place`(`uid`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        db.execSQL("CREATE TABLE IF NOT EXISTS `tempDay` (`day` INTEGER NOT NULL, `description` TEXT NOT NULL, `feel` TEXT, `image_name` TEXT, PRIMARY KEY(`day`))")

        db.execSQL("INSERT INTO tempActivity(uid, type_id, place_id, vehicle, description, favorite, image_name, feel, intensity, start_time, end_time, distance, wifi_bssid, track_preview) SELECT uid, type_id, place_id, vehicle, description, favorite, image_name, feel, intensity, start_time, end_time, distance, wifi_bssid, track_preview FROM Activity")
        db.execSQL("INSERT INTO tempDay SELECT * FROM Day")

        db.execSQL("DROP TABLE Activity")
        db.execSQL("DROP TABLE Day")

        db.execSQL("UPDATE tempActivity SET feel = 'Ok' WHERE feel IS NULL")
        db.execSQL("UPDATE tempDay SET feel = 'Ok' WHERE feel IS NULL")

        db.execSQL("CREATE TABLE IF NOT EXISTS `Activity` (`uid` INTEGER, `type_id` INTEGER NOT NULL, `place_id` INTEGER, `vehicle` TEXT, `description` TEXT NOT NULL, `favorite` INTEGER NOT NULL DEFAULT false, `image_name` TEXT, `feel` TEXT NOT NULL, `intensity` INTEGER, `start_time` INTEGER NOT NULL, `end_time` INTEGER NOT NULL, `distance` INTEGER, `wifi_bssid` TEXT, `track_preview` TEXT, PRIMARY KEY(`uid`), FOREIGN KEY(`type_id`) REFERENCES `ActivityType`(`uid`) ON UPDATE NO ACTION ON DELETE RESTRICT , FOREIGN KEY(`place_id`) REFERENCES `Place`(`uid`) ON UPDATE NO ACTION ON DELETE RESTRICT )")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Activity_type_id` ON `Activity` (`type_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Activity_place_id` ON `Activity` (`place_id`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_Activity_start_time` ON `Activity` (`start_time`)")
        db.execSQL("CREATE TABLE IF NOT EXISTS `Day` (`day` INTEGER NOT NULL, `description` TEXT NOT NULL, `feel` TEXT NOT NULL, `image_name` TEXT, PRIMARY KEY(`day`))")

        db.execSQL("INSERT INTO Activity SELECT * FROM tempActivity")
        db.execSQL("INSERT INTO Day SELECT * FROM tempDay")

        db.execSQL("DROP TABLE tempActivity")
        db.execSQL("DROP TABLE tempDay")
    }
}

val MIGRATION_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("DELETE FROM FilterHistoryItem")
    }
}

@DeleteColumn(tableName = "Activity", columnName = "wifi_bssid")
@DeleteColumn(tableName = "Recording", columnName = "wifi_bssid")
class Migration35To36Spec : AutoMigrationSpec
