package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.view_model.statistics.Period
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
sealed class Views(val nameId: Int) : Parcelable {
    @Serializable
    data object Home : Views(R.string.today)

    @Serializable
    data class EditDay(val primitiveEpochDay: Long) : Views(R.string.edit_day) {
        val epochDay get() = EpochDay(primitiveEpochDay)
    }

    @Serializable
    data class DayView(val primitiveEpochDay: Long = -1) : Views(R.string.days) {
        val epochDay
            get() =
                if (primitiveEpochDay == -1L) EpochDay.today() else EpochDay(primitiveEpochDay)
    }

    @Serializable
    data class ActivityLog(val primitiveActivityId: Int = -1) : Views(R.string.activity_log) {
        val activityId get() = if (primitiveActivityId == -1) null else primitiveActivityId
    }

    @Serializable
    data object FilterActivity : Views(R.string.filter)

    @Serializable
    data class NewActivity(val primitiveActivityId: Int = -1) : Views(R.string.new_activity) {
        val activityId get() = if (primitiveActivityId == -1) null else primitiveActivityId
    }

    @Serializable
    data object RecordActivity : Views(R.string.record_activity)

    @Serializable
    data object Settings : Views(R.string.settings)

    @Serializable
    data class Statistics(val primitiveInitialPeriod: String? = null) : Views(R.string.statistics) {
        val initialPeriod get() = primitiveInitialPeriod?.let { Period.valueOf(it) }
    }
}