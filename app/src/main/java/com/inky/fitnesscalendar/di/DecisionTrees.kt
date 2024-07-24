package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree
import com.inky.fitnesscalendar.util.decision_tree.Example
import com.inky.fitnesscalendar.util.decision_tree.Examples
import com.inky.fitnesscalendar.util.toLocalDateTime
import java.time.LocalDateTime

object DecisionTrees {
    var activityType: DecisionTree<ActivityType>? = null
        private set

    var vehicle: DecisionTree<Vehicle>? = null
        private set

    fun init(activities: List<RichActivity>) = synchronized(this) {
        activityType = learnActivityType(activities)
        vehicle = learnVehicle(activities)
    }

    private fun learnActivityType(
        activities: List<RichActivity>,
    ): DecisionTree<ActivityType> {
        val examples = Examples(activities.map {
            val attributes = attributes(it.activity.startTime.toLocalDateTime())
            Example(it.type, attributes)
        })

        return DecisionTree.learn(examples)
    }

    private fun learnVehicle(
        activities: List<RichActivity>,
    ): DecisionTree<Vehicle> {
        val examples = Examples(activities.map {
            val attributes = attributes(it.activity.startTime.toLocalDateTime())
            Example(it.activity.vehicle, attributes)
        })

        return DecisionTree.learn(examples)
    }

    private fun attributes(date: LocalDateTime): List<Any> {
        val hourOfDay = date.toLocalTime().hour
        // Segments:
        // 0: [2-6) Uhr
        // 1: [6-10) Uhr
        // 2: [10-14) Uhr
        // 3: [14-18) Uhr
        // 4: [18-22) Uhr
        // 5: [22-2) Uhr
        val timeOfDay =
            ((22.0 + hourOfDay.toDouble()).mod(24.0) / 4).toInt()
        val weekDay = date.dayOfWeek
        return listOf(timeOfDay, weekDay)
    }

    fun <T : Any> DecisionTree<T>.classifyNow() = classify(attributes(LocalDateTime.now()))
}
