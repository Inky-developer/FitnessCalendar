package com.inky.fitnesscalendar.data

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R

enum class ContentColor(@ColorRes val colorId: Int, @StringRes val nameId: Int) {
    Color1(R.color.content_1, R.string.color_orange),
    Color2(R.color.content_2, R.string.color_green),
    Color3(R.color.content_3, R.string.color_turquoise),
    Color4(R.color.content_4, R.string.color_brown),
    Color5(R.color.content_5, R.string.color_yellow),
    ColorOther(R.color.content_other, R.string.color_grey),
}