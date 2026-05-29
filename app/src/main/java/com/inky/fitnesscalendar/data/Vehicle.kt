package com.inky.fitnesscalendar.data

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.inky.fitnesscalendar.R

enum class Vehicle(@StringRes val nameId: Int, val emoji: String) {
    Train(R.string.vehicle_train, "🚆"),
    Car(R.string.vehicle_car, "🚗"),
    Bike(R.string.vehicle_bike, "🚴"),
    Foot(R.string.vehicle_walking, "🚶"),
    Plane(R.string.vehicle_plane, "✈️");

    @Composable
    fun text() = stringResource(nameId)
}