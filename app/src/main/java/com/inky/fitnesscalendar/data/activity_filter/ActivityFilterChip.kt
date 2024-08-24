package com.inky.fitnesscalendar.data.activity_filter

import android.content.Context
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.FilterHistoryItem
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichFilterHistoryItem

sealed class ActivityFilterChip {
    data class TextFilterChip(val text: String) : ActivityFilterChip()

    data class DateFilterChip(val option: DateRangeOption) : ActivityFilterChip()

    data class CategoryFilterChip(val category: ActivityCategory) : ActivityFilterChip()

    data class TypeFilterChip(val type: ActivityType) : ActivityFilterChip()

    data class PlaceFilterChip(val place: Place) : ActivityFilterChip()

    data class VehicleFilterChip(val vehicle: Vehicle) : ActivityFilterChip()

    data class AttributeFilterChip(val attribute: AttributeFilter.Attribute, val state: Boolean) :
        ActivityFilterChip()

    fun toFilterHistoryItem() = when (this) {
        is TextFilterChip -> FilterHistoryItem(type = FilterHistoryItem.ItemType.Text, text = text)
        is DateFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Date,
            dateRangeStart = option.range.start,
            dateRangeEnd = option.range.end,
            dateRangeName = option.name
        )

        is CategoryFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Category,
            category = category
        )

        is TypeFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Type,
            typeId = type.uid
        )

        is PlaceFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Place,
            placeId = place.uid
        )

        is VehicleFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Vehicle,
            vehicle = vehicle
        )

        is AttributeFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Attribute,
            attribute = attribute,
            attributeState = state
        )
    }

    fun removeFrom(filter: ActivityFilter) = when (this) {
        is AttributeFilterChip -> filter.copy(
            attributes = filter.attributes.with(attribute, AttributeFilter.TriState.Undefined)
        )

        is CategoryFilterChip -> filter.copy(categories = filter.categories.filter { it != category })
        is DateFilterChip -> filter.copy(range = null)
        is TextFilterChip -> filter.copy(text = null)
        is TypeFilterChip -> filter.copy(types = filter.types.filter { it != type })
        is PlaceFilterChip -> filter.copy(places = filter.places.filter { it != place })
        is VehicleFilterChip -> filter.copy(vehicles = filter.vehicles.filter { it != vehicle })
    }

    fun addTo(filter: ActivityFilter) = when (this) {
        is AttributeFilterChip -> filter.copy(
            attributes = filter.attributes.with(
                attribute,
                AttributeFilter.TriState.fromBoolean(state)
            )
        )

        is CategoryFilterChip -> filter.withCategory(category)
        is DateFilterChip -> filter.copy(range = option)
        is TextFilterChip -> filter.copy(text = text)
        is TypeFilterChip -> filter.withType(type)
        is PlaceFilterChip -> filter.withPlace(place)
        is VehicleFilterChip -> filter.withVehicle(vehicle)
    }

    fun displayText(context: Context) = when (this) {
        is AttributeFilterChip -> attribute.getString(context, state)
        is CategoryFilterChip -> context.getString(category.nameId)
        is DateFilterChip -> option.getText(context)
        is TextFilterChip -> text
        is TypeFilterChip -> type.name
        is PlaceFilterChip -> place.name
        is VehicleFilterChip -> context.getString(vehicle.nameId)
    }

    companion object {
        fun RichFilterHistoryItem.toActivityFilterChip(): ActivityFilterChip? {
            return when (item.type) {
                FilterHistoryItem.ItemType.Text -> TextFilterChip(text = item.text ?: return null)
                FilterHistoryItem.ItemType.Date -> DateFilterChip(
                    option = DateRangeOption(
                        range = DateRange(
                            start = item.dateRangeStart ?: return null,
                            end = item.dateRangeEnd
                        ),
                        name = item.dateRangeName
                    )
                )

                FilterHistoryItem.ItemType.Category -> CategoryFilterChip(
                    category = item.category ?: return null
                )

                FilterHistoryItem.ItemType.Type -> TypeFilterChip(type = type ?: return null)
                FilterHistoryItem.ItemType.Place -> PlaceFilterChip(place = place ?: return null)
                FilterHistoryItem.ItemType.Vehicle -> VehicleFilterChip(
                    vehicle = item.vehicle ?: return null
                )

                FilterHistoryItem.ItemType.Attribute -> AttributeFilterChip(
                    attribute = item.attribute ?: return null,
                    state = item.attributeState ?: return null
                )

            }
        }
    }
}