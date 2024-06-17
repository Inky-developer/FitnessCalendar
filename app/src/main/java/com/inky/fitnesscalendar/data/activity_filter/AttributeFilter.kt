package com.inky.fitnesscalendar.data.activity_filter

import android.content.Context
import android.os.Parcelable
import androidx.annotation.StringRes
import com.inky.fitnesscalendar.R
import kotlinx.parcelize.Parcelize

/**
 * Filters activities by whether they have certain attributes, for example by whether they have an image attached.
 */
@Parcelize
data class AttributeFilter(
    val description: TriState = TriState.Undefined,
    val feel: TriState = TriState.Undefined,
    val image: TriState = TriState.Undefined,
) : Parcelable {
    fun with(attribute: Attribute, value: TriState) = when (attribute) {
        Attribute.Description -> copy(description = value)
        Attribute.Feel -> copy(feel = value)
        Attribute.Image -> copy(image = value)
    }

    fun get(attribute: Attribute): TriState = when (attribute) {
        Attribute.Description -> description
        Attribute.Feel -> feel
        Attribute.Image -> image
    }

    fun entries() = Attribute.entries.map { it to get(it) }

    enum class Attribute(@StringRes val nameId: Int) {
        Description(R.string.description),
        Feel(R.string.feel),
        Image(R.string.image);

        fun getString(context: Context, state: Boolean): String {
            val filterStringId = if (state) {
                R.string.with_attribute
            } else {
                R.string.without_attribute
            }
            return context.getString(filterStringId, context.getString(nameId))
        }
    }

    enum class TriState {
        Yes,
        No,
        Undefined;

        fun toBooleanOrNull() = when (this) {
            Yes -> true
            No -> false
            Undefined -> null
        }
    }
}