package com.inky.fitnesscalendar.data

import android.content.Context
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R

enum class Feel(@StringRes val nameId: Int, val emoji: String, @ColorRes val colorId: Int) :
    Displayable {
    Bad(R.string.feel_bad, "🙁", R.color.feel_bad),
    Ok(R.string.feel_ok, "🙂", R.color.feel_ok),
    Good(R.string.feel_good, "😀", R.color.feel_good);

    override fun getText(context: Context) = context.getString(nameId)

    override fun getShortText() = emoji

    override fun getColor(context: Context) = context.getColor(colorId)


}