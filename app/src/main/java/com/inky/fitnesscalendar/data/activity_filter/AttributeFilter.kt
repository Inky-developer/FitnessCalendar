package com.inky.fitnesscalendar.data.activity_filter

import android.content.res.Resources
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
    val vehicle: TriState = TriState.Undefined,
    val image: TriState = TriState.Undefined,
    val place: TriState = TriState.Undefined,
    val track: TriState = TriState.Undefined,
) : Parcelable {
    fun with(attribute: Attribute, value: TriState) = when (attribute) {
        Attribute.Description -> copy(description = value)
        Attribute.Vehicle -> copy(vehicle = value)
        Attribute.Image -> copy(image = value)
        Attribute.Place -> copy(place = value)
        Attribute.Track -> copy(track = value)
    }

    fun get(attribute: Attribute): TriState = when (attribute) {
        Attribute.Description -> description
        Attribute.Vehicle -> vehicle
        Attribute.Image -> image
        Attribute.Place -> place
        Attribute.Track -> track
    }

    fun entries() = Attribute.entries.map { it to get(it) }

    enum class Attribute(@StringRes val nameId: Int) {
        Description(R.string.description),
        Vehicle(R.string.vehicle),
        Image(R.string.image),
        Place(R.string.place),
        Track(R.string.track);

        fun getString(context: Resources, state: Boolean): String {
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

        companion object {
            fun fromBoolean(value: Boolean) = when (value) {
                true -> Yes
                false -> No
            }
        }
    }
}