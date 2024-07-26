package com.inky.fitnesscalendar.ui.views.settings

import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsViews {
    @Serializable
    object Main

    @Serializable
    object Debug

    @Serializable
    object ActivityType

    @Serializable
    object PlaceList

    @Serializable
    data class PlaceDialog(val primitivePlaceId: Int = -1) {
        val placeId get() = if (primitivePlaceId == -1) null else primitivePlaceId
    }

    @Serializable
    object ImportExport
}