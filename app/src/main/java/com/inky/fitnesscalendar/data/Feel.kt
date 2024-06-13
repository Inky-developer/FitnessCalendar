package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class Feel(val nameId: Int, val emoji: String) {
    Bad(R.string.feel_bad, "🙁"),
    Ok(R.string.feel_ok, "🙂"),
    Good(R.string.feel_good, "😀"),
}