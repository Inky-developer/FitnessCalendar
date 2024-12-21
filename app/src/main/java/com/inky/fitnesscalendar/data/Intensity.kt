package com.inky.fitnesscalendar.data

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.ui.util.ContextFormat
import kotlinx.parcelize.Parcelize

@Parcelize
data class Intensity(val value: Byte) : Parcelable, ContextFormat {
    init {
        assert(value in 0..10)
    }

    override fun formatWithContext(context: Context) = toString()
}