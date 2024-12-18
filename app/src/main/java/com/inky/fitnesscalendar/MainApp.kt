package com.inky.fitnesscalendar

import android.app.Application
import android.util.Log
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.di.DecisionTrees
import com.inky.fitnesscalendar.repository.AutoImportRepository
import com.inky.fitnesscalendar.repository.DatabaseRepository
import com.inky.fitnesscalendar.util.cleanImageStorage
import com.inky.fitnesscalendar.util.getOrCreateImagesDir
import com.inky.fitnesscalendar.util.getOrCreateSharedMediaCache
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApp : Application() {
    @Inject
    lateinit var databaseRepository: DatabaseRepository

    @Inject
    lateinit var autoImportRepository: AutoImportRepository

    override fun onCreate() {
        super.onCreate()

        MainScope().launch(Dispatchers.IO) {
            initializeData()
            cleanupStorage()
            autoImportActivities()
        }
    }

    private suspend fun cleanupStorage() {
        val imagesDir = getOrCreateImagesDir().toPath()
        val usedImages = databaseRepository.getUsedImages().map { it.resolve(imagesDir) }
        cleanImageStorage(usedImages.toSet())

        getOrCreateSharedMediaCache().listFiles()?.forEach {
            it.delete()
        }
    }

    private suspend fun initializeData() {
        Log.i("MainApp", "Initializing app data")

        val activities = databaseRepository.loadMostRecentActivities(200)

        ActivityTypeOrder.init(activities)
        DecisionTrees.init(activities)
        Log.i("MainApp", "App data successfully initialized")
    }

    private suspend fun autoImportActivities() {
        Log.i("MainApp", "Auto-importing activities")
        autoImportRepository.performAutoImport()
        Log.i("MainApp", "Auto-importing activities done")
    }
}