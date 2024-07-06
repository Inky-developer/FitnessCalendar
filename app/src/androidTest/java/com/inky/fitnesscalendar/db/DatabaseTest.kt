package com.inky.fitnesscalendar.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    /**
     * Tests that the default data get loaded correctly
     */
    @Test
    fun test_load_default_data() {
        val context = ApplicationProvider.getApplicationContext<Context>()
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
        assertEquals(activityTypes.size, DefaultActivityType.entries.size)
        for (activityType in DefaultActivityType.entries) {
            assertNotNull(
                "Activity type got created",
                activityTypes.find { it.name == context.getString(activityType.titleId) })
        }
    }
}