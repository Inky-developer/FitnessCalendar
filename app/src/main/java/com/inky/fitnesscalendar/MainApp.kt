package com.inky.fitnesscalendar

import android.app.Application
import android.util.Log
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MainApp : Application() {
    @Inject
    lateinit var appRepository: AppRepository

    override fun onCreate() {
        super.onCreate()

        MainScope().launch {
            Log.i("MainApp", "Initializing app data")

            val activities = appRepository.loadMostRecentActivities(200)

            ActivityTypeOrder.init(activities)
            ActivityTypeDecisionTree.decisionTree = DecisionTree.learnFromActivities(activities)
            Log.i("MainApp", "App data successfully initialized")
        }
    }
}