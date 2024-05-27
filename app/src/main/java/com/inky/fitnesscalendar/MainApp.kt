package com.inky.fitnesscalendar

import android.app.Application
import com.inky.fitnesscalendar.di.ActivityTypeDecisionTree
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
            val activities = appRepository.loadMostRecentActivities(200)
            ActivityTypeDecisionTree.decisionTree = DecisionTree.learnFromActivities(activities)
        }
    }
}