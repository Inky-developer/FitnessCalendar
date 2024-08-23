package com.inky.fitnesscalendar.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import java.time.Instant
import java.util.Date

private const val TAG = "import_export"

fun Context.exportCsv(activities: List<RichActivity>) {
    val csvData =
        CSVWriter(listOf("uid", "type", "vehicle", "description", "start", "end", "feel")) {
            rows(activities) {
                listOf(
                    it.activity.uid,
                    it.type.name,
                    it.activity.vehicle,
                    it.activity.description,
                    it.activity.startTime,
                    it.activity.endTime,
                    it.activity.feel
                )
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

fun importCsv(rawData: String, types: List<ActivityType>): List<RichActivity> {
    val typeMap = types.associate { it.name to it.uid!! }
    val data = readCSV(rawData)

    println("Raw data: $data")

    return data.mapNotNull { row -> getActivity(row, typeMap) }.map { activity ->
        // TODO: Support importing activities with place and track
        RichActivity(
            type = types.first { it.uid == activity.typeId },
            place = null,
            activity = activity
        ).clean()
    }.also {
        Log.i(TAG, "Data to import: $it")
    }
}

private fun getActivity(data: Map<String, String?>, typeMap: Map<String, Int>): Activity? {
    val uid = data["uid"]?.toInt()
    val typeId = typeMap[data["type"]]
    if (typeId == null) {
        Log.i(TAG, "Skipped activity with unknown type ${data["type"]}")
        return null
    }
    val vehicle = data["vehicle"]?.let { Vehicle.valueOf(it) }
    val start = Date.from(Instant.ofEpochMilli(data["start"]?.toLong() ?: return null))
    val end = Date.from(Instant.ofEpochMilli(data["end"]?.toLong() ?: return null))
    val description = data["description"] ?: ""
    val feel = data["feel"]?.let { Feel.valueOf(it) }
    return Activity(
        uid = uid,
        typeId = typeId,
        vehicle = vehicle,
        startTime = start,
        endTime = end,
        description = description,
        feel = feel
    )
}