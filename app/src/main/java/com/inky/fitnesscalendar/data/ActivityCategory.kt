package com.inky.fitnesscalendar.data

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R

enum class ActivityCategory(
    @StringRes val nameId: Int,
    val emoji: String,
    @ColorRes val colorId: Int
) : Displayable {
    Sports(R.string.sports, "⛹️", R.color.stats_sport),
    Travel(R.string.travel, "🧳", R.color.stats_travel),
    Work(R.string.work, "💼", R.color.stats_work),
    Entertainment(R.string.entertainment, "🍿", R.color.stats_entertainment),
    Other(R.string.other, "🏷️", R.color.stats_other);

    override fun getColor(context: Context) = context.getColor(colorId)

    override fun getText(context: Context) = context.getString(nameId)
}