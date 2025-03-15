package com.inky.fitnesscalendar.repository

import android.os.Build
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.data.measure.HeartFrequency
import com.inky.fitnesscalendar.data.measure.Temperature
import com.inky.fitnesscalendar.data.measure.meters
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.repository.backup.BackupRepository
import com.inky.fitnesscalendar.testUtils.mockDatabase
import com.inky.fitnesscalendar.util.DATABASE_NAME
import com.inky.fitnesscalendar.util.toDate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.time.Period
import java.time.ZoneId
import kotlin.random.Random

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest {
    @Test
    fun testRestore() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val originalDb = mockDatabase(context).also(::addDefaultActivities)
        val originalActivities = runBlocking { originalDb.activityDao().loadActivities() }
        val originalTypes = runBlocking { originalDb.activityTypeDao().loadTypes() }

        val backupFile = createBackupFile(BackupRepository(originalDb, context))

        val emptyDb = mockDatabase(context)
        val repo = BackupRepository(emptyDb, context)
        repo.restoreWithoutRestart(backupFile.toUri())

        val restoredDbFile = context.getDatabasePath(DATABASE_NAME)
        val copyOfRestoredDb = File.createTempFile("restored-database", ".sqlite")
        restoredDbFile.copyTo(copyOfRestoredDb, overwrite = true)

        val restoredDB = Room.databaseBuilder(context, AppDatabase::class.java, "restored-db")
            .createFromFile(copyOfRestoredDb).allowMainThreadQueries().build()

        val restoredActivities = runBlocking { restoredDB.activityDao().loadActivities() }
        val restoredTypes = runBlocking { restoredDB.activityTypeDao().loadTypes() }
        println(restoredActivities)
        println(restoredTypes)
        assertEquals("Activities should be equal", originalActivities, restoredActivities)
        assertEquals("Activity types should be equal", originalTypes, restoredTypes)
    }

    private fun createBackupFile(repo: BackupRepository): File {
        val backupFile = File.createTempFile("backup", ".zip")
        repo.backup(backupFile)
        return backupFile
    }

    private fun addDefaultActivities(db: AppDatabase) = runBlocking {
        val startDay = LocalDate.of(2025, 1, 1)
        val endDay = LocalDate.of(2025, 6, 1)

        val random = Random(42)

        val activityTypes = db.activityTypeDao().loadTypes()

        val sports = activityTypes.filter { it.name == "Cycling" || it.name == "Running" }

        for (day in startDay.datesUntil(endDay, Period.ofDays(2))) {
            val startTime = day.atTime(random.nextInt(19), random.nextInt(60))
            val endTime = startTime.plusMinutes(random.nextLong(60 * 4))
            val activityType = sports.random(random)
            val activity = Activity(
                typeId = activityType.uid!!,
                startTime = startTime.toDate(ZoneId.of("UTC")),
                endTime = endTime.toDate(ZoneId.of("UTC")),
                description = "Some ${activityType.name}",
                favorite = random.nextDouble() < 0.05,
                distance = random.nextDouble(8000.0, 80000.0).meters(),
                temperature = Temperature(celsius = random.nextDouble(-10.0, 40.0)),
                averageHeartRate = HeartFrequency(bpm = random.nextDouble(120.0, 170.0)),
                maximalHeartRate = HeartFrequency(bpm = random.nextDouble(150.0, 200.0)),
            )
            db.activityDao().save(activity)
        }
    }
}
