package com.inky.fitnesscalendar.util

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun ZipInputStream.entries(): Iterator<ZipEntry> = iterator {
    var entry = nextEntry
    while (entry != null) {
        yield(entry)
        entry = nextEntry
    }
}