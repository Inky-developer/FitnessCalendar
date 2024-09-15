package com.inky.fitnesscalendar.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.net.toUri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.BACKUP_CACHE_FILE
import com.inky.fitnesscalendar.util.BACKUP_DB_NAME
import com.inky.fitnesscalendar.util.SDK_MIN_VERSION_FOR_SQLITE_VACUUM
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
    enum class BackupError(@StringRes val msgID: Int) {
        OLD_ANDROID_VERSION(R.string.your_android_version_is_too_old_for_backup)
    }

    fun backup(target: Uri): BackupError? {
        if (!isBackupSupported()) {
            return BackupError.OLD_ANDROID_VERSION
        }

        val zipFile = File(context.cacheDir, BACKUP_CACHE_FILE)

        Zip(zipFile).use { zip ->
            backupDatabase(zip)
            backupImages(zip)
        }

        context.copyFile(zipFile.toUri(), target)

        return null
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