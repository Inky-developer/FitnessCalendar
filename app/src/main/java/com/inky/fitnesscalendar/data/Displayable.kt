package com.inky.fitnesscalendar.data

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

interface Displayable {
    fun getText(context: Context): String

    fun getShortText(): String

    fun getColor(context: Context): Int

    @Composable
    fun text(): String = getText(LocalContext.current)

    @Composable
    fun color(): Color = Color(getColor(LocalContext.current))
}