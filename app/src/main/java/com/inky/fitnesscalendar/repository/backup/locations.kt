package com.inky.fitnesscalendar.repository.backup

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.DATABASE_NAME
import com.inky.fitnesscalendar.util.ZipWriter
import com.inky.fitnesscalendar.util.entries
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

private const val TAG = "BackupRepository"

internal val BACKUP_LOCATIONS = arrayOf(DatabaseLocation, ImagesLocation)

internal sealed interface BackupLocation {
    fun backup(context: Context, database: AppDatabase, zip: ZipWriter)

    // TODO: Use a better restore api. For example, there can be no entries after images right now, because
    // they would be consumed by the ImagesLocation restore method
    fun restore(
        context: Context,
        database: AppDatabase,
        zip: ZipInputStream
    ): BackupRepository.RestoreError?
}

data object DatabaseLocation : BackupLocation {
    override fun backup(context: Context, database: AppDatabase, zip: ZipWriter) {
        val dbFile = File(context.cacheDir, BACKUP_DB_NAME).apply {
            delete()
        }
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(dbFile.toPath().toString()))
        database.query(query).use {
            it.moveToFirst()
        }

        zip.addFile(dbFile)
    }

    override fun restore(
        context: Context,
        database: AppDatabase,
        zip: ZipInputStream
    ): BackupRepository.RestoreError? {
        val entry = zip.nextEntry ?: return BackupRepository.RestoreError.IOError
        if (entry.name != BACKUP_DB_NAME || entry.isDirectory) return BackupRepository.RestoreError.IOError

        database.close()
        val dbPath = context.getDatabasePath(DATABASE_NAME)
        FileOutputStream(dbPath).use { output ->
            output.write(zip.readBytes())
        }

        return null
    }
}

data object ImagesLocation : BackupLocation {
    private const val BACKUP_IMAGES_PATH = "images"

    override fun backup(context: Context, database: AppDatabase, zip: ZipWriter) {
        val dir = context.getOrCreateImagesDir()
        for (file in dir.listFiles() ?: return) {
            zip.addFile(file, BACKUP_IMAGES_PATH)
        }
    }

    override fun restore(
        context: Context,
        database: AppDatabase,
        zip: ZipInputStream
    ): BackupRepository.RestoreError? {
        val dir = context.getOrCreateImagesDir()
        dir.deleteRecursively()
        dir.mkdir()

        zip.entries { entry ->
            if (!entry.name.startsWith(BACKUP_IMAGES_PATH)) return null
            if (entry.isDirectory) return@entries

            val filename = entry.name.removePrefix("$BACKUP_IMAGES_PATH/")

            val imageFile = File(dir, filename)
            Log.i(TAG, "Restoring image at ${imageFile.path}")
            context.contentResolver.openOutputStream(imageFile.toUri()).use { output ->
                output!!.write(zip.readBytes())
            }
        }

        return null
    }
}