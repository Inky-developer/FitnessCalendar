package com.inky.fitnesscalendar.repository

import android.content.Context
import android.os.Build
import androidx.core.net.toUri
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.generateSampleActivities
import com.inky.fitnesscalendar.repository.backup.BackupRepository
import com.inky.fitnesscalendar.testUtils.mockDatabase
import com.inky.fitnesscalendar.util.DATABASE_NAME
import com.inky.fitnesscalendar.util.copyFileToStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class BackupRepositoryTest {
    @Test
    fun testRestore() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()

        val originalDb = mockDatabase(context).also(::generateSampleActivities)
        val originalActivities = runBlocking { originalDb.activityDao().loadActivities() }
        val originalTypes = runBlocking { originalDb.activityTypeDao().loadTypes() }

        createTestImages(context)
        assertEquals(16, context.getOrCreateImagesDir().listFiles()!!.size)

        val backupFile = createBackupFile(BackupRepository(originalDb, context))

        context.getOrCreateImagesDir().deleteRecursively()
        assertEquals(0, context.getOrCreateImagesDir().listFiles()!!.size)

        val emptyDb = mockDatabase(context)
        val repo = BackupRepository(emptyDb, context)
        repo.restoreWithoutRestart(backupFile.toUri())

        assertEquals(16, context.getOrCreateImagesDir().listFiles()!!.size)

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

    private fun createTestImages(context: Context) {
        for (i in 0..15) {
            val sourceImageFile = File.createTempFile("image$i", ".png")
            sourceImageFile.writeText("Very realistic image data $i")
            context.copyFileToStorage(sourceImageFile.toUri(), context.getOrCreateImagesDir())
        }
    }
}
