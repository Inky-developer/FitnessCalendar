package com.inky.fitnesscalendar.view_model.activity_log

import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.RichActivity

sealed class ActivityListItem(val contentType: Int) {
    data class Activity(val richActivity: RichActivity) : ActivityListItem(0)

    data class DateHeader(val day: Day) : ActivityListItem(1)

}