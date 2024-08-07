package com.inky.fitnesscalendar.data.activity_filter

import android.os.Parcelable
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import kotlinx.parcelize.Parcelize


@Parcelize
data class ActivityFilter(
    val types: List<ActivityType> = emptyList(),
    val categories: List<ActivityCategory> = emptyList(),
    val places: List<Place> = emptyList(),
    val text: String? = null,
    val range: DateRangeOption? = null,
    val attributes: AttributeFilter = AttributeFilter(),
) : Parcelable {
    fun isEmpty() = this == ActivityFilter()

    fun withCategory(newCategory: ActivityCategory): ActivityFilter {
        val newSelection =
            categories.filter { it != newCategory }.toMutableList()
        newSelection.add(newCategory)
        return copy(categories = newSelection)
    }

    fun withType(newType: ActivityType): ActivityFilter {
        val newSelection = types.filter { it != newType }.toMutableList()
        newSelection.add(newType)
        return copy(types = newSelection)
    }

    fun withPlace(newPlace: Place): ActivityFilter {
        val newSelection = places.filter { it != newPlace }.toMutableList()
        newSelection.add(newPlace)
        return copy(places = newSelection)
    }

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