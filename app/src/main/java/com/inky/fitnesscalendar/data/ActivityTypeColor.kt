package com.inky.fitnesscalendar.data

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R

// TODO: Specify color names
enum class ActivityTypeColor(@ColorRes val colorId: Int, @StringRes val nameId: Int) {
    Color1(R.color.stats_1, R.string.color_orange),
    Color2(R.color.stats_2, R.string.color_green),
    Color3(R.color.stats_3, R.string.color_turquoise),
    Color4(R.color.stats_4, R.string.color_brown),
    Color5(R.color.stats_5, R.string.color_yellow),
    ColorOther(R.color.stats_other, R.string.color_grey),
}