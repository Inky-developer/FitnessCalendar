package com.inky.fitnesscalendar.di

import android.content.Context
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.repository.AutoImportRepository
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.ImportRepository
import com.inky.fitnesscalendar.repository.RecordingRepository
import com.inky.fitnesscalendar.repository.backup.BackupRepository

class AppContext(
    val context: Context,
    database: AppDatabase = AppDatabase.getInstance(context),
) {
    val localizationRepository = LocalizationRepository(context)
    val databaseRepo = DatabaseRepository(context, database, localizationRepository)
    val recordingRepository = RecordingRepository(
        context,
        database.recordingDao(),
        database.activityTypeDao(),
        database.activityDao(),
    )
    val importRepository = ImportRepository(databaseRepo)
    val autoImportRepository = AutoImportRepository(importRepository, context)
    val backupRepository = BackupRepository(database, context)
}

val Context.appContext: AppContext
    get() = (applicationContext as MainApp).app
