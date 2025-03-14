package com.inky.fitnesscalendar.repository.backup

import android.content.Context
import android.util.Log
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.DATABASE_NAME
import com.inky.fitnesscalendar.util.ZipWriter
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry

private const val TAG = "BackupRepository"

internal val BACKUP_LOCATIONS = arrayOf(DatabaseLocation, ImagesLocation).associateBy { it.key }

internal sealed interface BackupLocation {
    val key: String

    fun backup(context: Context, database: AppDatabase, zip: ZipWriter)

    fun restore(
        context: Context,
        database: AppDatabase,
    ): BackupRestorer

    interface BackupRestorer {
        fun processEntry(entry: ZipEntry, data: () -> ByteArray): BackupRepository.RestoreError?
    }
}

internal data object DatabaseLocation : BackupLocation {
    override val key = "database"

    override fun backup(context: Context, database: AppDatabase, zip: ZipWriter) {
        val dbFile = File(context.cacheDir, BACKUP_DB_NAME).apply {
            delete()
        }
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(dbFile.toPath().toString()))
        database.query(query).use {
            it.moveToFirst()
        }

        zip.addFile(dbFile, directory = key)
    }

    override fun restore(
        context: Context,
        database: AppDatabase,
    ): BackupLocation.BackupRestorer {
        database.close()

        return object : BackupLocation.BackupRestorer {
            override fun processEntry(
                entry: ZipEntry,
                data: () -> ByteArray
            ): BackupRepository.RestoreError? {
                val dbPath = context.getDatabasePath(DATABASE_NAME)
                FileOutputStream(dbPath).use { output ->
                    output.write(data())
                }

                return null
            }
        }
    }
}

internal data object ImagesLocation : BackupLocation {
    override val key = "images"

    override fun backup(context: Context, database: AppDatabase, zip: ZipWriter) {
        val dir = context.getOrCreateImagesDir()
        for (file in dir.listFiles() ?: return) {
            zip.addFile(file, directory = key)
        }
    }

    override fun restore(
        context: Context,
        database: AppDatabase,
    ): BackupLocation.BackupRestorer {
        val dir = context.getOrCreateImagesDir()
        dir.deleteRecursively()
        dir.mkdir()

        return object : BackupLocation.BackupRestorer {
            override fun processEntry(
                entry: ZipEntry,
                data: () -> ByteArray
            ): BackupRepository.RestoreError? {
                val filename = entry.name.removePrefix("$key/")

                val imageFile = File(dir, filename)
                Log.i(TAG, "Restoring image at ${imageFile.path}")
                context.contentResolver.openOutputStream(imageFile.toUri()).use { output ->
                    output!!.write(data())
                }

                return null
            }
        }
    }
}