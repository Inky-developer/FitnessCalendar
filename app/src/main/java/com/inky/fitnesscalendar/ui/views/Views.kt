package com.inky.fitnesscalendar.ui.views

import android.os.Parcelable
import com.inky.fitnesscalendar.R
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.util.DEEP_LINK_BASE_PATH
import com.inky.fitnesscalendar.view_model.statistics.Period
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable


@Parcelize
@Serializable
sealed class Views : Parcelable {
    abstract val nameId: Int

    @Serializable
    data object Home : Views() {
        override val nameId get() = R.string.today
    }

    @Serializable
    data class EditDay(val primitiveEpochDay: Long) : Views() {
        override val nameId get() = R.string.edit_day

        val epochDay get() = EpochDay(primitiveEpochDay)
    }

    @Serializable
    data class DayView(val primitiveEpochDay: Long? = null) : Views() {
        override val nameId get() = R.string.days

        val epochDay
            get() = primitiveEpochDay?.let { EpochDay(primitiveEpochDay) } ?: EpochDay.today()
    }

    @Serializable
    data class ActivityLog(val activityId: Int? = null) : Views() {
        override val nameId get() = R.string.activity_log
    }

    @Serializable
    data object FilterActivity : Views() {
        override val nameId get() = R.string.filter
    }

    @Serializable
    data class NewActivity(val activityId: Int? = null, val rawInitialStartDay: Long? = null) :
        Views() {
        override val nameId get() = R.string.new_activity

        val initialStartDay: EpochDay? get() = rawInitialStartDay?.let { EpochDay(it) }
    }

    @Serializable
    data class TrackDetails(val activityId: Int) : Views() {
        override val nameId get() = R.string.track_details
    }

    @Serializable
    data class TrackGraph(val activityId: Int, val projection: TrackGraphProjection) :
        Views() {
        override val nameId get() = R.string.track_graph
    }

    @Serializable
    data class Map(val activityId: Int) : Views() {
        override val nameId get() = R.string.Map
    }

    @Serializable
    data object SummaryView : Views() {
        override val nameId get() = R.string.summary
    }

    @Serializable
    data object RecordActivity : Views() {
        override val nameId get() = R.string.record_activity
    }

    @Serializable
    data object Settings : Views() {
        override val nameId get() = R.string.settings
    }

    @Serializable
    data class Statistics(val primitiveInitialPeriod: String? = null) : Views() {
        override val nameId get() = R.string.statistics

        val initialPeriod get() = primitiveInitialPeriod?.let { Period.valueOf(it) }
    }
}