package com.inky.fitnesscalendar.repository

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Immutable
import androidx.room.withTransaction
import com.inky.fitnesscalendar.data.EpochDay
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.ImageName
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilter
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.ActivityFilterChip.Companion.toActivityFilterChip
import com.inky.fitnesscalendar.data.activity_filter.AttributeFilter
import com.inky.fitnesscalendar.data.activity_filter.DateRange
import com.inky.fitnesscalendar.data.activity_filter.DateRangeOption
import com.inky.fitnesscalendar.db.AppDatabase
import com.inky.fitnesscalendar.db.entities.Activity
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.ActivityTypeName
import com.inky.fitnesscalendar.db.entities.Day
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.db.entities.Track
import com.inky.fitnesscalendar.db.generateSampleActivities
import com.inky.fitnesscalendar.di.ActivityTypeOrder
import com.inky.fitnesscalendar.util.Ordering
import com.inky.fitnesscalendar.util.toEpochDay
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Immutable
@Singleton
class DatabaseRepository @Inject constructor(
    @ApplicationContext val context: Context,
    private val db: AppDatabase,
) {
    private val activityDao = db.activityDao()
    private val activityTypeDao = db.activityTypeDao()
    private val filterHistoryDao = db.filterHistoryDao()
    private val activityTypeNameDao = db.activityTypeNameDao()
    private val trackDao = db.trackDao()
    private val dayDao = db.dayDao()
    private val placeDao = db.placeDao()

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
        val newActivityId = activityDao.save(activity.clean().activity).toInt()
        val activityId = activity.activity.uid ?: newActivityId
        activityDao.setActivityImages(activityId, activity.images)
        return activityId
    }

    suspend fun deleteActivity(activity: Activity) {
        activityDao.delete(activity)
    }

    suspend fun loadMostRecentActivities(n: Int) = activityDao.loadMostRecentActivities(n)

    fun getActivity(id: Int) = activityDao.get(id)

    suspend fun loadActivity(id: Int) = activityDao.load(id)

    suspend fun getUsedImages(): Set<ImageName> =
        activityDao.getImages().toSet() + dayDao.getImages() + placeDao.getImages()

    suspend fun saveActivityType(activityType: ActivityType) = activityTypeDao.save(activityType)

    suspend fun deleteActivityType(activityType: ActivityType) =
        activityTypeDao.delete(activityType)

    fun getActivityTypes() = activityTypeDao.getTypes()

    fun getActivityTypeRows() =
        getActivityTypesByCategory().map { ActivityTypeOrder.getRowsOrDefault(it) }

    private fun getActivityTypesByCategory() = activityTypeDao.getActivityTypesByCategory()

    suspend fun upsertFilterHistoryChips(chips: List<ActivityFilterChip>) =
        db.withTransaction {
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

    fun getDaysFiltered(filter: ActivityFilter): Flow<Set<EpochDay>> {
        // Instead of reading the attributes directly from the filter, we
        // loop over the filter items and return an empty set if we don't know that item.
        // This is to make sure that we don't include days that should be filtered out
        // by some filter that days don't support.
        var searchText: String? = null
        val feels: MutableList<Feel> = mutableListOf()
        var start: EpochDay? = null
        var end: EpochDay? = null
        var hasImage: Boolean? = null
        for (chip in filter.items()) {
            when (chip) {
                is ActivityFilterChip.TextFilterChip -> searchText = chip.text
                is ActivityFilterChip.FeelFilterChip -> feels.add(chip.feel)
                is ActivityFilterChip.DateFilterChip -> {
                    start = chip.option.range.start.toEpochDay()
                    chip.option.range.end?.let { end = it.toEpochDay() }
                }

                is ActivityFilterChip.AttributeFilterChip -> if (chip.attribute == AttributeFilter.Attribute.Image) {
                    hasImage = chip.state
                } else {
                    return flowOf(emptySet())
                }

                else -> return flowOf(emptySet())
            }
        }

        return dayDao.getDaysFiltered(
            search = searchText?.let { "%$it%" },
            feels = feels,
            isFeelEmpty = feels.isEmpty(),
            start = start,
            end = end,
            hasImage = hasImage
        ).map { it.toSet() }
    }

    fun getDay(day: EpochDay): Flow<Day> = dayDao.get(day).map { it ?: Day(day = day) }

    fun getDayActivities(day: EpochDay) =
        getActivities(
            ActivityFilter(range = DateRangeOption(DateRange.atDay(day))),
            order = Ordering.ASC
        )

    suspend fun saveDay(day: Day) {
        dayDao.upsertOrDelete(day)
    }

    fun getPlace(id: Int) = placeDao.get(id)

    suspend fun savePlace(place: Place) = placeDao.upsert(place)

    suspend fun deletePlace(place: Place) = placeDao.delete(place)

    fun getPlaces() = placeDao.getAll()

    fun getActivityTypeNames() =
        activityTypeNameDao
            .getAll()
            .map { names -> names.associate { it.typeName.name to it.type } }

    suspend fun setActivityTypeName(name: String, type: ActivityType) = activityTypeNameDao.set(
        ActivityTypeName(name, type.uid!!)
    )

    suspend fun saveTrack(track: Track) = trackDao.upsert(track)

    suspend fun loadTracks() = trackDao.loadAll()

    fun getTrackByActivity(activityId: Int) = trackDao.getByActivityId(activityId)

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    fun generateSampleActivitiesForTesting() = generateSampleActivities(db)
}