package com.inky.fitnesscalendar.data

import android.content.Context

interface Displayable {
    fun getText(context: Context): String

    fun getShortText(): String

    fun getColor(context: Context): Int
}