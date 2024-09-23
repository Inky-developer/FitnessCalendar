package com.inky.fitnesscalendar.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.room.withTransaction
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.DateRange
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.dao.ActivityDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeDao
import com.inky.fitnesscalendar.db.dao.ActivityTypeNameDao
import com.inky.fitnesscalendar.db.dao.DayDao
import com.inky.fitnesscalendar.db.dao.FilterHistoryDao
import com.inky.fitnesscalendar.db.dao.PlaceDao
import com.inky.fitnesscalendar.db.dao.RecordingDao
import com.inky.fitnesscalendar.db.dao.TrackDao
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.ActivityTypeName
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.Recording
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.RichRecording
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.localization.LocalizationRepository
import com.inky.fitnesscalendar.preferences.Preference
import com.inky.fitnesscalendar.util.Ordering
import com.inky.fitnesscalendar.util.getCurrentBssid
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
class DatabaseRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val database: AppDatabase,
    private val activityDao: ActivityDao,
    private val recordingDao: RecordingDao,
    private val activityTypeDao: ActivityTypeDao,
    private val filterHistoryDao: FilterHistoryDao,
    private val activityTypeNameDao: ActivityTypeNameDao,
    private val trackDao: TrackDao,
    private val dayDao: DayDao,
    private val placeDao: PlaceDao,
    val localizationRepository: LocalizationRepository
) {
    fun getActivities(
        filter: ActivityFilter,
        order: Ordering = Ordering.DESC
    ): Flow<List<RichActivity>> {
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

        val hasDescription = filter.attributes.description.toBooleanOrNull()
        val hasVehicle = filter.attributes.vehicle.toBooleanOrNull()
        val hasImage = filter.attributes.image.toBooleanOrNull()
        val hasPlace = filter.attributes.place.toBooleanOrNull()
        val hasTrack = filter.attributes.track.toBooleanOrNull()

        return activityDao.getFiltered(
            order = order.ordinal,
            typeIds = filter.types.mapNotNull { it.uid },
            isTypesEmpty = filter.types.isEmpty(),
            categories = categories,
            isCategoriesEmpty = categories.isEmpty(),
            placeIds = filter.places.mapNotNull { it.uid },
            isPlacesEmpty = filter.places.isEmpty(),
            vehicles = filter.vehicles,
            isVehiclesEmpty = filter.vehicles.isEmpty(),
            feels = filter.feels,
            isFeelsEmpty = filter.feels.isEmpty(),
            favorite = filter.favorite,
            search = filter.text?.let { "%$it%" },
            searchVehicles = searchVehicles,
            start = filter.range?.range?.start,
            end = filter.range?.range?.end,
            hasDescription = hasDescription,
            hasVehicle = hasVehicle,
            hasImage = hasImage,
            hasPlace = hasPlace,
            hasTrack = hasTrack,
        )
    }

    fun getMostRecentActivity() = activityDao.getMostRecentActivity()

    suspend fun saveActivity(activity: RichActivity): Int {
        return activityDao.save(activity.clean().activity).toInt()
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    suspend fun loadMostRecentActivities(n: Int) = activityDao.loadMostRecentActivities(n)

    fun getActivity(id: Int) = activityDao.get(id)

    suspend fun loadActivity(id: Int) = activityDao.load(id)

    suspend fun getUsedImages(): Set<ImageName> =
        activityDao.getImages().toSet() + dayDao.getImages()

    suspend fun startRecording(richRecording: RichRecording, context: Context) {
        val includeWifi = Preference.COLLECT_BSSID.get(context)
        val recording = if (includeWifi) {
            richRecording.recording.copy(wifiBssid = context.getCurrentBssid())
        } else {
            richRecording.recording
        }
        val recordingId = recordingDao.insert(recording).toInt()
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

    fun getDayActivities(day: EpochDay) =
        getActivities(
            ActivityFilter(range = DateRangeOption(DateRange.atDay(day))),
            order = Ordering.ASC
        )

    suspend fun saveDay(day: Day) {
        dayDao.upsert(day)
    }

    fun getPlace(id: Int) = placeDao.get(id)

    suspend fun savePlace(place: Place) = placeDao.upsert(place)

    suspend fun deletePlace(place: Place) = placeDao.delete(place)

    fun getPlaces() = placeDao.getAll()

    fun getActivityCountPerPlace() = placeDao.getWithActivityCount()

    fun getActivityTypeNames() =
        activityTypeNameDao
            .getAll()
            .map { names -> names.associate { it.typeName.name to it.type } }

    suspend fun setActivityTypeName(name: String, type: ActivityType) = activityTypeNameDao.set(
        ActivityTypeName(name, type.uid!!)
    )

    suspend fun saveTrack(track: Track) = trackDao.upsert(track)

    suspend fun loadTracks() = trackDao.loadAll()
}