package com.inky.fitnesscalendar.util

import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.OutputStream
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.path.name

class ZipWriter(output: OutputStream) : Closeable {
    private val zipStream = ZipOutputStream(output.buffered())

    fun addFile(file: File, directory: String = "", compressed: Boolean = true) {
        assert(file.isFile) { "Recursion should not be required right now" }
        val filename = (directory + "/" + file.toPath().name).trimStart('/')
        zipStream.setLevel(if (compressed) Deflater.DEFAULT_COMPRESSION else Deflater.NO_COMPRESSION)
        FileInputStream(file).buffered().use { inputStream ->
            zipStream.putNextEntry(ZipEntry(filename))
            inputStream.copyTo(zipStream)
        }
    }

    override fun close() {
        zipStream.close()
    }
}
