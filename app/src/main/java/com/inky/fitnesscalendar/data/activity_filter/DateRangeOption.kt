package com.inky.fitnesscalendar.data.activity_filter

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize


@Parcelize
data class DateRangeOption(val range: DateRange, val name: DateRangeName? = null) : Parcelable {
    fun getText(context: Context) = name?.nameId?.let { context.getString(it) } ?: range.toString()

    enum class DateRangeName(@StringRes val nameId: Int) {
        Today(R.string.today),
        Yesterday(R.string.yesterday),
        SevenDays(R.string.week),
        LastWeek(R.string.last_week),
        FourWeeks(R.string.four_weeks),
        ThirtyDays(R.string.month),
        LastMonth(R.string.last_month),
        Year(R.string.year),
        LastYear(R.string.last_year);
    }

    companion object {
        fun today() = DateRangeOption(range = DateRange.atDay(0), name = DateRangeName.Today)
        fun yesterday() =
            DateRangeOption(range = DateRange.atDay(-1), name = DateRangeName.Yesterday)

        fun sevenDays() =
            DateRangeOption(range = DateRange.lastDays(7), name = DateRangeName.SevenDays)

        fun lastWeek() =
            DateRangeOption(range = DateRange.lastDays(14, 7), name = DateRangeName.LastWeek)

        fun fourWeeks() =
            DateRangeOption(range = DateRange.lastDays(28), name = DateRangeName.FourWeeks)

        fun thirtyDays() =
            DateRangeOption(range = DateRange.lastDays(30), name = DateRangeName.ThirtyDays)

        fun lastMonth() =
            DateRangeOption(range = DateRange.lastDays(60, 30), name = DateRangeName.LastMonth)

        fun year() = DateRangeOption(range = DateRange.lastDays(365), name = DateRangeName.Year)
        fun lastYear() =
            DateRangeOption(range = DateRange.lastDays(365 * 2, 365), name = DateRangeName.LastYear)
    }
}