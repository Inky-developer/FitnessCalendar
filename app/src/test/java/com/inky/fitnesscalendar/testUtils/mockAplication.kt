package com.inky.fitnesscalendar.testUtils

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ContentColor
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.di.AppRepository
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.repository.LocalizationRepositoryImpl
import com.inky.fitnesscalendar.repository.RecordingRepository
import com.inky.fitnesscalendar.ui.util.ProvideDatabaseValues
import kotlinx.coroutines.runBlocking

// Uids start at 100 to avoid colliding with auto-generated uids when
// callers also load default data via mockDatabase's onCreate hook.
val SportActivityType = ActivityType(
    uid = 100,
    activityCategory = ActivityCategory.Sports,
    name = "Sports",
    emoji = "S",
    color = ContentColor.Color1,
    hasPlace = true,
    archived = false,
)
val ProgrammingActivityType = ActivityType(
    uid = 101,
    activityCategory = ActivityCategory.Work,
    name = "Programming",
    emoji = "P",
    color = ContentColor.Color2,
    archived = false
)

val mockActivityTypes = listOf(
    SportActivityType,
    ProgrammingActivityType
)

@Composable
fun MockApplication(content: @Composable context(AppRepository) () -> Unit) {
    val app = mockAppRepository(LocalContext.current)
    context(app) {
        ProvideDatabaseValues(app.db) {
            content()
        }
    }
}


fun mockAppRepository(
    context: Context,
    onCreate: (SupportSQLiteDatabase) -> Unit = {}
): AppRepository {
    val db = mockDatabase(context, onCreate)
    val app = AppRepository(
        context = context,
        db = DatabaseRepository(context, db),
        localizationRepository = LocalizationRepositoryImpl(context),
        recordingRepository = RecordingRepository(context, db)
    )
    for (activityType in mockActivityTypes) {
        runBlocking { app.db.saveActivityType(activityType) }
    }
    return app
}

fun mockDatabase(context: Context, onCreate: (SupportSQLiteDatabase) -> Unit = {}): AppDatabase {
    return Room
        .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
        .allowMainThreadQueries()
        .addCallback(object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                onCreate(db)
            }
        })
        .build()
}
