package com.inky.fitnesscalendar.data

import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R


enum class DateRangeOption(@StringRes val nameId: Int) {
    Today(R.string.today),
    Yesterday(R.string.yesterday),
    SevenDays(R.string.week),
    LastWeek(R.string.last_week),
    ThirtyDays(R.string.month),
    LastMonth(R.string.last_month),
    Year(R.string.year),
    LastYear(R.string.last_year);

    fun getDateRange() = when (this) {
        Today -> DateRange.atDay(0)
        Yesterday -> DateRange.atDay(-1)
        SevenDays -> DateRange.lastDays(7)
        LastWeek -> DateRange.lastDays(14, 7)
        ThirtyDays -> DateRange.lastDays(30)
        LastMonth -> DateRange.lastDays(60, 30)
        Year -> DateRange.lastDays(365)
        LastYear -> DateRange.lastDays(365 * 2, 365)
    }
}