package com.inky.fitnesscalendar.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileNotFoundException
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TAG = "file_storage"

data class InternalFile(val name: String, val uri: Uri)

fun Context.copyFileToStorage(input: Uri, targetDir: File): InternalFile? {
    val filename = UUID.randomUUID().toString()
    val file = File(targetDir, filename)
    val result = copyFile(contentResolver, input, file.toUri())
    return if (result != null) {
        InternalFile(name = filename, uri = result)
    } else {
        null
    }
}

fun Context.copyFile(input: Uri, output: Uri): Uri? = copyFile(contentResolver, input, output)

private fun copyFile(contentResolver: ContentResolver, input: Uri, output: Uri): Uri? {
    try {
        return contentResolver.openInputStream(input).use { inputStream ->
            if (inputStream == null) {
                return null
            }

            contentResolver.openOutputStream(output).use { outputStream ->
                if (outputStream == null) {
                    return null
                }

                val bytesCopied = inputStream.copyTo(outputStream)

                Log.i(TAG, "Copied $bytesCopied bytes from $input to $output")

                output
            }
        }
    } catch (_: FileNotFoundException) {
        return null
    }
}

fun Context.getOrCreateImagesDir(): File {
    val dir = File(filesDir, IMAGES_DIR)
    dir.mkdir()
    return dir
}

fun Context.getOrCreateSharedMediaCache(): File {
    val dir = File(cacheDir, SHARED_MEDIA_DIR)
    dir.mkdir()
    return dir
}

/**
 * Returns the path for temporary backups and deletes any files currently existing at this path
 */
fun Context.getTemporaryBackupUri(): Uri {
    val file = File(cacheDir, "backup.sqlite")
    file.delete()
    return file.toUri()
}

fun Context.cleanImageStorage(liveUris: Set<Uri>) {
    val deadFiles = getOrCreateImagesDir().listFiles { file ->
        !liveUris.contains(file.toUri()) && System.currentTimeMillis() - file.lastModified() > ChronoUnit.DAYS.duration.toMillis()
    } ?: return
    Log.i(TAG, "Deleting ${deadFiles.size} dead file(s)")
    for (file in deadFiles) {
        file.delete()
    }
}