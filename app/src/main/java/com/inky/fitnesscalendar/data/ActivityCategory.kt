package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class ActivityCategory(val nameId: Int, val emoji: String, val colorId: Int) {
    Sports(R.string.sports, "⛹️", R.color.stats_sport),
    Travel(R.string.travel, "🧳", R.color.stats_travel),
    Work(R.string.work, "💼", R.color.stats_work),
    Gaming(R.string.gaming, "🎮", R.color.stats_gaming),
    Other(R.string.other, "🏷️", R.color.stats_other)
}