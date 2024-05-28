package com.inky.fitnesscalendar.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("UPDATE Activity SET type = \"UniversityCommute\" WHERE type = \"WorkCommute\"")
    }
}