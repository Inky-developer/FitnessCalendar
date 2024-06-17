package com.inky.fitnesscalendar.util

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import java.io.File
import java.io.FileNotFoundException
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val TAG = "file_storage"

fun Context.copyFileToStorage(input: Uri, targetDir: File): Uri? {
    try {
        return contentResolver.openInputStream(input).use { inputStream ->
            if (inputStream == null) {
                return@use null
            }
            val filename = UUID.randomUUID().toString()
            val file = File(targetDir, filename)
            file.outputStream().use { outputStream ->
                val bytesCopied = inputStream.copyTo(outputStream)

                Log.i(TAG, "Copied $bytesCopied bytes from $input to ${file.toUri()}")

                file.toUri()
            }
        }
    } catch (_: FileNotFoundException) {
        return null
    }
}

fun Context.getOrCreateActivityImagesDir(): File {
    val dir = File(filesDir, ACTIVITY_IMAGES_DIR)
    dir.mkdir()
    return dir
}

fun Context.cleanActivityImageStorage(liveUris: Set<Uri>) {
    val deadFiles = getOrCreateActivityImagesDir().listFiles { file ->
        !liveUris.contains(file.toUri()) && System.currentTimeMillis() - file.lastModified() > ChronoUnit.DAYS.duration.toMillis()
    } ?: return
    Log.i(TAG, "Deleting ${deadFiles.size} dead file(s)")
    for (file in deadFiles) {
        file.delete()
    }
}