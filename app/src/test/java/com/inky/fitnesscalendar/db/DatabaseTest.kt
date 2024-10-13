package com.inky.fitnesscalendar.db

import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.inky.fitnesscalendar.MainApp
import kotlinx.coroutines.runBlocking
import org.approvaltests.Approvals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.UPSIDE_DOWN_CAKE])
@RunWith(RobolectricTestRunner::class)
class DatabaseTest {
    /**
     * Tests that the default data get loaded correctly
     */
    @Test
    fun test_load_default_data() {
        val context = ApplicationProvider.getApplicationContext<MainApp>()
        val db = Room
            .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    loadDefaultData(db, context)
                }
            })
            .build()

        val activityTypeDao = db.activityTypeDao()
        val activityTypes = runBlocking { activityTypeDao.loadTypes() }
        Approvals.verifyAll("ActivityTypes", activityTypes)

        db.close()
    }
}