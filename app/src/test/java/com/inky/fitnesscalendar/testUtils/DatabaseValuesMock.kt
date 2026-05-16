package com.inky.fitnesscalendar.testUtils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.loadDefaultData
import com.inky.fitnesscalendar.di.AppContext
import com.inky.fitnesscalendar.ui.util.DatabaseValues
import com.inky.fitnesscalendar.ui.util.localDatabaseValues

val SportActivityType = ActivityType(
    uid = 1,
    activityCategory = ActivityCategory.Sports,
    name = "Sports",
    emoji = "S",
    color = ContentColor.Color1,
    hasPlace = true,
)
val ProgrammingActivityType = ActivityType(
    uid = 2,
    activityCategory = ActivityCategory.Work,
    name = "Programming",
    emoji = "P",
    color = ContentColor.Color2,
)

val mockActivityTypes = listOf(
    SportActivityType,
    ProgrammingActivityType
)

@Composable
fun MockDatabaseValues(content: @Composable () -> Unit) {
    CompositionLocalProvider(value = localDatabaseValues provides getDatabaseValues()) {
        content()
    }
}

fun mockDatabase(context: Context): AppDatabase {
    return Room
        .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                loadDefaultData(db, context)
            }
        })
        .build()
}


fun mockAppContext(context: Context, database: AppDatabase = mockDatabase(context)): AppContext =
    AppContext(context, database)

private fun getDatabaseValues(): DatabaseValues {
    val activityTypesByCategory = mockActivityTypes.groupBy { it.activityCategory }
    return DatabaseValues(
        activityTypes = mockActivityTypes,
        activityTypeRows = ActivityCategory.entries.map {
            activityTypesByCategory[it] ?: emptyList()
        },
        activityFilterChips = emptyList(),
        activityTypeNames = emptyMap(),
        places = emptyList()
    )
}