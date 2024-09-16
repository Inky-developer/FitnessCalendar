package com.inky.fitnesscalendar.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_CACHE_FILE
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.SDK_MIN_VERSION_FOR_SQLITE_VACUUM
import com.inky.fitnesscalendar.util.Zip
import com.inky.fitnesscalendar.util.copyFile
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import com.inky.fitnesscalendar.util.toLocalDateTime
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.util.Date
import javax.inject.Inject

const val BACKUP_IMAGES_PATH = "images"
const val BACKUP_NAME = "backup.zip"

class BackupRepository @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) {
    enum class BackupError(@StringRes val msgID: Int) {
        OldAndroidVersion(R.string.your_android_version_is_too_old_for_backup),
        CannotAccessFile(R.string.cannot_access_file)
    }

    fun getLastBackup(directory: Uri): LocalDateTime? {
        val backupFile =
            DocumentFile.fromTreeUri(context, directory)?.findFile(BACKUP_NAME) ?: return null

        val lastModified = backupFile.lastModified()
        return Date.from(Instant.ofEpochMilli(lastModified)).toLocalDateTime()
    }

    fun backup(directory: Uri): BackupError? {
        if (!isBackupSupported()) {
            return BackupError.OldAndroidVersion
        }

        val targetUri = prepareBackupFile(directory) ?: return BackupError.CannotAccessFile

        val zipFile = File(context.cacheDir, BACKUP_CACHE_FILE)

        Zip(zipFile).use { zip ->
            backupDatabase(zip)
            backupImages(zip)
        }

        context.copyFile(zipFile.toUri(), targetUri)

        return null
    }

    /**
     * Creates the backup file and clears any previous backup files
     */
    private fun prepareBackupFile(directory: Uri): Uri? {
        val dirFile = DocumentFile.fromTreeUri(context, directory) ?: return null
        dirFile.findFile(BACKUP_NAME)?.delete()
        return dirFile.createFile("application/zip", BACKUP_NAME)?.uri
    }

    private fun backupDatabase(zip: Zip) {
        val dbFile = File(context.cacheDir, BACKUP_DB_NAME).apply {
            delete()
        }
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(dbFile.toPath().toString()))
        database.query(query).use {
            it.moveToFirst()
        }

        zip.addFile(dbFile)
    }

    private fun backupImages(zip: Zip) {
        val dir = context.getOrCreateImagesDir()
        for (file in dir.listFiles() ?: return) {
            zip.addFile(file, BACKUP_IMAGES_PATH)
        }
    }

    companion object {
        fun isBackupSupported() = Build.VERSION.SDK_INT >= SDK_MIN_VERSION_FOR_SQLITE_VACUUM
    }
}