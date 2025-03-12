package com.inky.fitnesscalendar.repository.backup

import android.content.Context
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.Zip
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import java.io.File

internal val BACKUP_LOCATIONS = arrayOf(DatabaseLocation, ImagesLocation)

internal sealed interface BackupLocation {
    fun backup(context: Context, database: AppDatabase, zip: Zip)

    fun restore(context: Context, zip: Zip)
}

data object DatabaseLocation : BackupLocation {
    override fun backup(context: Context, database: AppDatabase, zip: Zip) {
        val dbFile = File(context.cacheDir, BACKUP_DB_NAME).apply {
            delete()
        }
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(dbFile.toPath().toString()))
        database.query(query).use {
            it.moveToFirst()
        }

        zip.addFile(dbFile)
    }

    override fun restore(context: Context, zip: Zip) {
        TODO("Not yet implemented")
    }
}

data object ImagesLocation : BackupLocation {
    override fun backup(context: Context, database: AppDatabase, zip: Zip) {
        val dir = context.getOrCreateImagesDir()
        for (file in dir.listFiles() ?: return) {
            zip.addFile(file, BACKUP_IMAGES_PATH)
        }
    }

    override fun restore(context: Context, zip: Zip) {
        TODO("Not yet implemented")
    }
}