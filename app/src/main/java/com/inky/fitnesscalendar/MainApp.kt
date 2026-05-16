package com.inky.fitnesscalendar

import android.app.Application
import android.util.Log
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.di.AppContext
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.util.cleanImageStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class MainApp : Application() {
    lateinit var app: AppContext
        private set

    override fun onCreate() {
        super.onCreate()

        app = AppContext(this)

        MainScope().launch(Dispatchers.IO) {
            initializeData()
            cleanupStorage()
            autoImportActivities()
        }
    }

    private suspend fun cleanupStorage() {
        val imagesDir = getOrCreateImagesDir().toPath()
        val usedImages = app.databaseRepo.getUsedImages().map { it.resolve(imagesDir) }
        cleanImageStorage(usedImages.toSet())

        getOrCreateSharedMediaCache().listFiles()?.forEach {
            it.delete()
        }
    }

    private suspend fun initializeData() {
        Log.i("MainApp", "Initializing app data")

        val activities = app.databaseRepo.loadMostRecentActivities(200)

        ActivityTypeOrder.init(activities)
        DecisionTrees.init(activities)
        Log.i("MainApp", "App data successfully initialized")
    }

    private suspend fun autoImportActivities() {
        Log.i("MainApp", "Auto-importing activities")
        app.autoImportRepository.performAutoImport()
        Log.i("MainApp", "Auto-importing activities done")
    }
}
