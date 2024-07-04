package com.inky.fitnesscalendar.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.TypeActivity
import java.time.Instant
import java.util.Date

private const val TAG = "import_export"

fun Context.exportCsv(activities: List<Activity>) {
    val csvData =
        CSVWriter(listOf("typeUid", "typeId", "vehicle", "description", "start", "end", "feel")) {
            rows(activities) {
                listOf(
                    it.uid,
                    it.typeId,
                    it.vehicle,
                    it.description,
                    it.startTime,
                    it.endTime,
                    it.feel
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

fun importCsv(rawData: String, types: List<ActivityType>): List<TypeActivity> {
    val typeMap = types.associate { it.name to it.uid!! }
    val data = readCSV(rawData)
    Log.i(TAG, "Data to import: $data")
    return data.mapNotNull { row -> getActivity(row, typeMap) }.map { activity ->
        TypeActivity(
            type = types.first { it.uid == activity.typeId },
            activity = activity
        ).clean()
    }
}

private fun getActivity(data: Map<String, String?>, typeMap: Map<String, Int>): Activity? {
    val uid = data["typeUid"]?.toInt()
    val typeId = data["typeId"]?.let {
        it.toIntOrNull() ?: typeMap[it]
    } ?: return null
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