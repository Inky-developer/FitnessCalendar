package com.inky.fitnesscalendar.util

import android.content.Context
import android.content.Intent
import com.inky.fitnesscalendar.data.Activity

fun Context.exportCsv(activities: List<Activity>) {
    val csvData = CSVWriter(listOf("type", "vehicle", "description", "start", "end")) {
        rows(activities) {
            listOf(it.type, it.vehicle, it.description, it.startTime, it.endTime)
        }
    }.toString()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csvData)
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}