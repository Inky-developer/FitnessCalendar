package com.inky.fitnesscalendar.util

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.name

class ZipWriter(output: File) : Closeable {
    private val zipStream = ZipOutputStream(FileOutputStream(output))

    fun addFile(file: File, directory: String = "") {
        assert(file.isFile) { "Recursion should not be required right now" }
        val filename = (directory + "/" + file.toPath().name).trimStart('/')
        FileInputStream(file).use { inputStream ->
            zipStream.putNextEntry(ZipEntry(filename))
            inputStream.copyTo(zipStream)
        }
    }

    override fun close() {
        zipStream.close()
    }
}