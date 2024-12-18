package com.inky.fitnesscalendar.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.documentfile.provider.DocumentFile
import com.inky.fitnesscalendar.ImportActivity
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_WATCHED_FOLDERS
import com.inky.fitnesscalendar.preferences.Preference.Companion.PREF_WATCHED_FOLDERS_LAST_IMPORT
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.FileNotFoundException
import java.time.Instant
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AutoImportRepository"

@Immutable
@Singleton
class AutoImportRepository @Inject constructor(
    private val importRepository: ImportRepository,
    @ApplicationContext private val context: Context
) {

    /**
     * Auto imports from all configured directories.
     *
     * This method should not be called while it's running
     */
    suspend fun performAutoImport() {
        val dirs = PREF_WATCHED_FOLDERS.get(context)
        importFrom(dirs, false)
    }

    suspend fun importFrom(dirs: Set<Uri>, forceImport: Boolean) {
        val lastImportDate =
            if (forceImport) Date(0) else PREF_WATCHED_FOLDERS_LAST_IMPORT.get(context)
        val currentImportDate = Date.from(Instant.now())

        val newFiles = dirs.flatMap { dir ->
            val dirFile = DocumentFile.fromTreeUri(context, dir) ?: return@flatMap emptyList()
            collectPendingFiles(dirFile, lastImportDate, currentImportDate)
        }
        val uris = newFiles.map { file -> file.uri }

        if (uris.isNotEmpty()) {
            importUris(uris)
        }

        PREF_WATCHED_FOLDERS_LAST_IMPORT.set(context, currentImportDate)
    }

    private suspend fun importUris(uris: List<Uri>) {
        val fileDescriptors = uris.mapNotNull { uri ->
            try {
                context.contentResolver.openFileDescriptor(uri, "r")
            } catch (_: FileNotFoundException) {
                null
            }
        }
        val success = importRepository.tryImportFiles(fileDescriptors)

        if (!success) {
            Log.i(
                TAG,
                "Could not import activities automatically, user interaction is required"
            )
            val intent = Intent(context, ImportActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                action = Intent.ACTION_SEND_MULTIPLE
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
            }
            context.startActivity(intent)
        }
    }

    private fun collectPendingFiles(
        dir: DocumentFile,
        from: Date,
        until: Date
    ): List<DocumentFile> =
        dir.listFiles().filter { file ->
            val lastModified = Date(file.lastModified())
            lastModified.after(from) && !lastModified.after(until)
        }
}