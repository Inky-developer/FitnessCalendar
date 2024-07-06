package com.inky.fitnesscalendar.view_model.activity_log

import com.inky.fitnesscalendar.db.entities.TypeActivity
import java.time.LocalDate

sealed class ActivityListItem(val contentType: Int) {
    data class Activity(val typeActivity: TypeActivity) : ActivityListItem(0)

    data class DateHeader(val date: LocalDate) : ActivityListItem(1)

}