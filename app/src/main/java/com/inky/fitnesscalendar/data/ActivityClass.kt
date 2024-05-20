package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class ActivityClass(val nameId: Int, val emoji: String) {
    Sports(R.string.sports, "⛹️"),
    Travel(R.string.travel, "🧳"),
    Other(R.string.other, "🏷️")
}