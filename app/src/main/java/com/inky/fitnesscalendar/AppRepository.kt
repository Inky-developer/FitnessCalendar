package com.inky.fitnesscalendar

import com.inky.fitnesscalendar.data.Activity
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.localization.LocalizationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    private val activityDao: ActivityDao,
    val localizationRepository: LocalizationRepository
) {
    fun getActivities(): Flow<List<Activity>> = activityDao.getAll()

    suspend fun saveActivity(activity: Activity) {
        activityDao.save(activity.clean())
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    fun getActivity(id: Int) = activityDao.get(id)
}