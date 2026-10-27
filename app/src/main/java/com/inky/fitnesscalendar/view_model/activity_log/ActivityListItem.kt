package com.inky.fitnesscalendar.view_model.activity_log

import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.RichDay

sealed class ActivityListItem(val contentType: Int) {
    data class Activity(val richActivity: RichActivity) : ActivityListItem(0)

    data class DateHeader(val day: RichDay) : ActivityListItem(1)

}