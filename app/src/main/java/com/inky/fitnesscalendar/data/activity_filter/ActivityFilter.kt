package com.inky.fitnesscalendar.data.activity_filter

import android.os.Parcelable
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.Feel
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.util.added
import kotlinx.parcelize.Parcelize


@Parcelize
data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val categories: List<ActivityCategory> = emptyList(),
    val places: List<Place> = emptyList(),
    val vehicles: List<Vehicle> = emptyList(),
    val feels: List<Feel> = emptyList(),
    val favorite: Boolean? = null,
    val text: String? = null,
    val range: DateRangeOption? = null,
    val attributes: AttributeFilter = AttributeFilter(),
) : Parcelable {
    fun normalize(): ActivityFilter {
        if (types.isEmpty()) {
            return this
        }
        val validPlaceColors = types.mapNotNull { it.limitPlacesByColor }.toSet()
        println(validPlaceColors)
        return copy(places = places.filter { validPlaceColors.contains(it.color) })
    }

    fun isEmpty() = this == ActivityFilter()

    fun withCategory(newCategory: ActivityCategory) =
        copy(categories = categories.added(newCategory))

    fun withType(newType: ActivityType) = copy(types = types.added(newType))

    fun withPlace(newPlace: Place) = copy(places = places.added(newPlace))

    fun withVehicle(newVehicle: Vehicle) = copy(vehicles = vehicles.added(newVehicle))

    fun withFeel(newFeel: Feel) = copy(feels = feels.added(newFeel))

    fun items(): List<ActivityFilterChip> {
        val items = mutableListOf<ActivityFilterChip>()

        if (!text.isNullOrBlank()) {
            items.add(ActivityFilterChip.TextFilterChip(text))
        }

        if (range != null) {
            items.add(ActivityFilterChip.DateFilterChip(range))
        }

        for (category in categories) {
            items.add(ActivityFilterChip.CategoryFilterChip(category))
        }

        for (type in types) {
            items.add(ActivityFilterChip.TypeFilterChip(type))
        }

        for (place in places) {
            items.add(ActivityFilterChip.PlaceFilterChip(place))
        }

        for (vehicle in vehicles) {
            items.add(ActivityFilterChip.VehicleFilterChip(vehicle))
        }

        for (feel in feels) {
            items.add(ActivityFilterChip.FeelFilterChip(feel))
        }

        if (favorite != null) {
            items.add(ActivityFilterChip.FavoriteFilterChip(favorite))
        }

        for ((attribute, state) in attributes.entries()
            .filter { it.second != AttributeFilter.TriState.Undefined }) {
            items.add(
                ActivityFilterChip.AttributeFilterChip(
                    attribute,
                    state.toBooleanOrNull()!!
                )
            )
        }

        return items
    }
}