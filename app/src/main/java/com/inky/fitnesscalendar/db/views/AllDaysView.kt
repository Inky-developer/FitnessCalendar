package com.inky.fitnesscalendar.db.views

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Embedded
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.db.entities.Day

/**
 * View for all days with any data associated on them
 */
@DatabaseView(
    """
        SELECT keys.day AS epoch_day, Day.* FROM (SELECT day FROM Day UNION SELECT day FROM Steps) AS keys
        LEFT JOIN Day ON Day.day = keys.day
        """
)
class AllDaysView(
    @ColumnInfo(name = "epoch_day") val epochDay: EpochDay,
    @Embedded val day: Day?
)