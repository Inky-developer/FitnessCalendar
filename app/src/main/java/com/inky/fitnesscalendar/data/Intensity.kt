package com.inky.fitnesscalendar.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Intensity(val value: Byte) : Parcelable {
    init {
        assert(value in 0..10)
    }
}