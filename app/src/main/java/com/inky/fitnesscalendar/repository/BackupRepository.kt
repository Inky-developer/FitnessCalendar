package com.inky.fitnesscalendar.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_CACHE_FILE
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.Zip
import com.inky.fitnesscalendar.util.copyFile
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

const val BACKUP_IMAGES_PATH = "images"

class BackupRepository @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) {
    fun backup(target: Uri) {
        val zipFile = File(context.cacheDir, BACKUP_CACHE_FILE)

        Zip(zipFile).use { zip ->
            backupDatabase(zip)
            backupImages(zip)
        }

        context.copyFile(zipFile.toUri(), target)
    }

    private fun backupDatabase(zip: Zip) {
        val dbFile = File(context.cacheDir, BACKUP_DB_NAME).apply {
            delete()
        }
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(dbFile.toPath().toString()))
        val result = database.query(query)
        result.moveToFirst()

        zip.addFile(dbFile)
    }

    private fun backupImages(zip: Zip) {
        val dir = context.getOrCreateImagesDir()
        for (file in dir.listFiles() ?: return) {
            zip.addFile(file, BACKUP_IMAGES_PATH)
        }
    }
}