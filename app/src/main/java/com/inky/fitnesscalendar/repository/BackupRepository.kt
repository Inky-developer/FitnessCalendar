package com.inky.fitnesscalendar.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import androidx.sqlite.db.SimpleSQLiteQuery
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.util.copyFile
import com.inky.fitnesscalendar.util.getTemporaryBackupUri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class BackupRepository @Inject constructor(
    private val database: AppDatabase,
    @ApplicationContext private val context: Context
) {
    fun backup(target: Uri) {
        val path = context.getTemporaryBackupUri()
        val query = SimpleSQLiteQuery("VACUUM INTO ?", arrayOf(path.toFile().toPath().toString()))
        val result = database.query(query)
        result.moveToFirst()

        context.copyFile(path, target)
    }
}