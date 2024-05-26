package com.inky.fitnesscalendar.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.data.ActivityType
import com.inky.fitnesscalendar.data.Vehicle
import java.time.Instant
import java.util.Date

const val TAG = "import_export"

fun Context.exportCsv(activities: List<Activity>) {
    val csvData = CSVWriter(listOf("uid", "type", "vehicle", "description", "start", "end")) {
        rows(activities) {
            listOf(it.uid, it.type, it.vehicle, it.description, it.startTime, it.endTime)
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

fun importCsv(rawData: String): List<Activity> {
    val data = readCSV(rawData)
    Log.i(TAG, "Data to import: $data")
    return data.mapNotNull { row -> getActivity(row) }
}

private fun getActivity(data: Map<String, String?>): Activity? {
    val uid = data["uid"]?.toInt()
    val type = ActivityType.valueOf(data["type"] ?: return null)
    val vehicle = data["vehicle"]?.let { Vehicle.valueOf(it) }
    val start = Date.from(Instant.ofEpochMilli(data["start"]?.toLong() ?: return null))
    val end = Date.from(Instant.ofEpochMilli(data["end"]?.toLong() ?: return null))
    val description = data["description"] ?: ""
    return Activity(
        uid = uid,
        type = type,
        vehicle = vehicle,
        startTime = start,
        endTime = end,
        description = description
    )
}