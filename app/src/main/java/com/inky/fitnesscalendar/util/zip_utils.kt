package com.inky.fitnesscalendar.util

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

inline fun ZipInputStream.entries(handler: (ZipEntry) -> Unit) {
    var entry = nextEntry
    while (entry != null) {
        handler(entry)
        entry = nextEntry
    }
}