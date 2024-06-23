package com.inky.fitnesscalendar.data.activity_filter

import android.content.Context
import com.inky.fitnesscalendar.data.ActivityCategory
import com.inky.fitnesscalendar.data.ActivityType

sealed class ActivityFilterChip {
    data class TextFilterChip(val text: String) : ActivityFilterChip()

    data class DateFilterChip(val option: DateRangeOption) : ActivityFilterChip()

    data class CategoryFilterChip(val category: ActivityCategory) : ActivityFilterChip()

    data class TypeFilterChip(val type: ActivityType) : ActivityFilterChip()

    data class AttributeFilterChip(val attribute: AttributeFilter.Attribute, val state: Boolean) :
        ActivityFilterChip()

    fun toFilterHistoryItem() = when (this) {
        is TextFilterChip -> FilterHistoryItem(type = FilterHistoryItem.ItemType.Text, text = text)
        is DateFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Date,
            dateRangeOption = option
        )

        is CategoryFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Category,
            category = category
        )

        is TypeFilterChip -> FilterHistoryItem(
            type = FilterHistoryItem.ItemType.Type,
            typeId = type.uid
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
    }

    fun displayText(context: Context) = when (this) {
        is AttributeFilterChip -> attribute.getString(context, state)
        is CategoryFilterChip -> context.getString(category.nameId)
        is DateFilterChip -> context.getString(option.nameId)
        is TextFilterChip -> text
        is TypeFilterChip -> type.name
    }

    companion object {
        fun FullFilterHistoryItem.toActivityFilterChip(): ActivityFilterChip? {
            return when (item.type) {
                FilterHistoryItem.ItemType.Text -> TextFilterChip(text = item.text ?: return null)
                FilterHistoryItem.ItemType.Date -> DateFilterChip(
                    option = item.dateRangeOption ?: return null
                )

                FilterHistoryItem.ItemType.Category -> CategoryFilterChip(
                    category = item.category ?: return null
                )

                FilterHistoryItem.ItemType.Type -> TypeFilterChip(type = type ?: return null)
                FilterHistoryItem.ItemType.Attribute -> AttributeFilterChip(
                    attribute = item.attribute ?: return null,
                    state = item.attributeState ?: return null
                )

            }
        }
    }
}