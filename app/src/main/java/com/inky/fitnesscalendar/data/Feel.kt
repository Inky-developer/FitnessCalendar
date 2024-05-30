package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class Feel(val nameId: Int, val emoji: String) {
    Good(R.string.feel_good, "😀"),
    Ok(R.string.feel_ok, "😐"),
    Bad(R.string.feel_bad, "🙁")
}