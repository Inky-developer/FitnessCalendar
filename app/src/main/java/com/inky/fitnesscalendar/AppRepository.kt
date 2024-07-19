package com.inky.fitnesscalendar

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.withTransaction
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.DayDao
import com.inky.fitnesscalendar.db.dao.FilterHistoryDao
import com.inky.fitnesscalendar.db.dao.PlaceDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.util.hideRecordingNotification
import com.inky.fitnesscalendar.util.showRecordingNotification
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "AppRepository"

@Immutable
@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val database: AppDatabase,
    private val activityDao: ActivityDao,
    private val recordingDao: RecordingDao,
    private val activityTypeDao: ActivityTypeDao,
    private val filterHistoryDao: FilterHistoryDao,
    private val dayDao: DayDao,
    private val placeDao: PlaceDao,
    val localizationRepository: LocalizationRepository
) {
    suspend fun loadAllActivities() = activityDao.loadActivities()

    fun getActivities(filter: ActivityFilter): Flow<List<RichActivity>> {
        // Use simpler query if no filters are needed
        if (filter.isEmpty()) {
            return activityDao.getActivities()
        }

        val searchVehicles = filter.text?.let { filterText ->
            Vehicle.entries.filter { vehicle ->
                context.getString(vehicle.nameId).contains(filterText, ignoreCase = true)
            }
        } ?: emptyList()

        val categories = filter.categories.map { it.toString() }

        val rangeDate = filter.range?.getDateRange()

        val hasDescription = filter.attributes.description.toBooleanOrNull()
        val hasFeel = filter.attributes.feel.toBooleanOrNull()
        val hasImage = filter.attributes.image.toBooleanOrNull()

        return activityDao.getFiltered(
            typeIds = filter.types.mapNotNull { it.uid },
            isTypesEmpty = filter.types.isEmpty(),
            categories = categories,
            isCategoriesEmpty = categories.isEmpty(),
            placeIds = filter.places.mapNotNull { it.uid },
            isPlacesEmpty = filter.places.isEmpty(),
            search = filter.text?.let { "%$it%" },
            searchVehicles = searchVehicles,
            start = rangeDate?.start,
            end = rangeDate?.end,
            hasDescription = hasDescription,
            hasFeel = hasFeel,
            hasImage = hasImage
        )
    }

    fun getMostRecentActivity() = activityDao.getMostRecentActivity()

    suspend fun saveActivity(activity: RichActivity) {
        activityDao.save(activity.clean().activity)
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    suspend fun loadMostRecentActivities(n: Int) = activityDao.loadMostRecentActivities(n)

    fun getActivity(id: Int) = activityDao.get(id)

    suspend fun getActivityImages() = activityDao.getImages()

    suspend fun startRecording(richRecording: RichRecording, context: Context) {
        val recordingId = recordingDao.insert(richRecording.recording).toInt()
        context.showRecordingNotification(
            recordingId,
            richRecording.type,
            richRecording.recording.startTime.time
        )
    }

    fun getRecordings() = recordingDao.getRecordings()

    suspend fun getRecording(uid: Int) = recordingDao.getById(uid)

    suspend fun deleteRecording(recording: Recording) {
        Log.d(TAG, "Deleting $recording")
        recording.uid?.let { context.hideRecordingNotification(it) }
        recordingDao.delete(recording)
    }

    suspend fun endRecording(recording: Recording) {
        Log.d(TAG, "Ending recording $recording")
        recording.uid?.let { context.hideRecordingNotification(it) }
        val type = getActivityType(recording.typeId)
        if (type == null) {
            Log.e(TAG, "Could not retrieve type for activity")
            return
        }
        val activity = recording.toActivity(type)
        activityDao.stopRecording(recording, activity)
    }

    suspend fun saveActivityType(activityType: ActivityType) = activityTypeDao.save(activityType)

    suspend fun deleteActivityType(activityType: ActivityType) =
        activityTypeDao.delete(activityType)

    suspend fun loadActivityTypes() = activityTypeDao.loadTypes()

    fun getActivityTypes() = activityTypeDao.getTypes()

    private suspend fun getActivityType(id: Int): ActivityType? = activityTypeDao.get(id)

    fun getActivityTypeRows() =
        getActivityTypesByCategory().map { ActivityTypeOrder.getRowsOrDefault(it) }

    private fun getActivityTypesByCategory() = activityTypeDao.getActivityTypesByCategory()

    suspend fun upsertFilterHistoryChips(chips: List<ActivityFilterChip>) =
        database.withTransaction {
            val historyItems = getFilterHistoryItems().first()
                .associate { it.toActivityFilterChip()!! to it.item.uid!! }
            for (item in chips) {
                val existingItemId = historyItems[item]
                filterHistoryDao.upsert(
                    item.toFilterHistoryItem().copy(uid = existingItemId)
                )
            }
            filterHistoryDao.onlyKeepNewest(8)
        }

    fun getFilterHistoryItems() = filterHistoryDao.getItems()

    fun getDays() = dayDao.getDays()

    fun getDay(day: EpochDay): Flow<Day> = dayDao.get(day).map { it ?: Day(day = day) }

    suspend fun saveDay(day: Day) {
        dayDao.upsert(day)
    }

    suspend fun savePlace(place: Place) = placeDao.upsert(place)

    suspend fun deletePlace(place: Place) = placeDao.delete(place)

    fun getPlaces() = placeDao.getAll()
}