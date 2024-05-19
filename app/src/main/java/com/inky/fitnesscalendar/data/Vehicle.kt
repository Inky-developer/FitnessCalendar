package com.inky.fitnesscalendar.data

import com.inky.fitnesscalendar.R

enum class Vehicle(val nameId: Int, val emoji: String) {
    Train(R.string.vehicle_train, "🚆"),
    Car(R.string.vehicle_car, "🚗"),
    Bike(R.string.vehicle_bike, "🚴"),
    Foot(R.string.vehicle_walking, "🚶")
}