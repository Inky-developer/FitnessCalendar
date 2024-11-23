package com.inky.fitnesscalendar.testUtils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.ui.util.DatabaseValues
import com.inky.fitnesscalendar.ui.util.localDatabaseValues

val mockActivityTypes = listOf(
    ActivityType(
        uid = 1,
        activityCategory = ActivityCategory.Sports,
        name = "Sports",
        emoji = "S",
        color = ContentColor.Color1,
        hasPlace = true,
    ),
    ActivityType(
        uid = 2,
        activityCategory = ActivityCategory.Work,
        name = "Programming",
        emoji = "P",
        color = ContentColor.Color2,
    ),
)

@Composable
fun MockDatabaseValues(content: @Composable () -> Unit) {
    CompositionLocalProvider(value = localDatabaseValues provides getDatabaseValues()) {
        content()
    }
}

private fun getDatabaseValues(): DatabaseValues {
    val activityTypesByCategory = mockActivityTypes.groupBy { it.activityCategory }
    return DatabaseValues(
        activityTypes = mockActivityTypes,
        activityTypeRows = ActivityCategory.entries.map {
            activityTypesByCategory[it] ?: emptyList()
        }
    )
}