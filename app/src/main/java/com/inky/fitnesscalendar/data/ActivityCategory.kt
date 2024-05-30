package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class ActivityCategory(val nameId: Int, val emoji: String) {
    Sports(R.string.sports, "⛹️"),
    Travel(R.string.travel, "🧳"),
    Work(R.string.work, "💼"),
    Gaming(R.string.gaming, "🎮"),
    Other(R.string.other, "🏷️")
}