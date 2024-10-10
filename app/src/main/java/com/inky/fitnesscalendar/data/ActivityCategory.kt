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
    Sports(R.string.sports, "⛹️", R.color.category_sport),
    Travel(R.string.travel, "🧳", R.color.category_travel),
    Work(R.string.work, "💼", R.color.category_work),
    Entertainment(R.string.entertainment, "🍿", R.color.category_entertainment),
    Other(R.string.other, "🏷️", R.color.content_other);

    override fun getColor(context: Context) = context.getColor(colorId)

    override fun getText(context: Context) = context.getString(nameId)

    override fun getShortText() = emoji
}