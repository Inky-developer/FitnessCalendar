package com.inky.fitnesscalendar.ui.views.settings

import kotlinx.serialization.Serializable

@Serializable
sealed interface SettingsViews {
    @Serializable
    object Main

    @Serializable
    object About

    @Serializable
    object OpenSourceLicences

    @Serializable
    object Debug

    @Serializable
    object ActivityType

    @Serializable
    object PlaceList

    @Serializable
    data class PlaceDialog(val placeId: Int? = null)

    @Serializable
    object Backup
}