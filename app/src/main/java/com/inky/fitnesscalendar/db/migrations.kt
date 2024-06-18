package com.inky.fitnesscalendar.db

import androidx.room.RenameColumn
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE Activity SET type = \"UniversityCommute\" WHERE type = \"WorkCommute\"")
    }
}

@RenameColumn(tableName = "Activity", fromColumnName = "type", toColumnName = "type_id")
class Migration4To5Spec: AutoMigrationSpec