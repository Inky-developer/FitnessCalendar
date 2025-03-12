package com.inky.fitnesscalendar.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.inky.fitnesscalendar.MainActivity
import kotlin.system.exitProcess

fun restartApplication(context: Context): Nothing {
    val componentName = ComponentName(context, MainActivity::class.java)
    val restartIntent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(restartIntent)
    exitProcess(0)
}