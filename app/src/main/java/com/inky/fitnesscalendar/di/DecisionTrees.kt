package com.inky.fitnesscalendar.di

import android.content.Context
import android.os.Parcelable
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree
import com.inky.fitnesscalendar.util.decision_tree.Example
import com.inky.fitnesscalendar.util.decision_tree.Examples
import com.inky.fitnesscalendar.util.getCurrentBssid
import com.inky.fitnesscalendar.util.toLocalDateTime
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

object DecisionTrees {
    @Parcelize
    data class Prediction(
        val activityType: ActivityType?,
        val vehicle: Vehicle?,
        val place: Place?
    ) : Parcelable

    var activityType: DecisionTree<ActivityType>? = null
        private set
    var vehicle: DecisionTree<Vehicle>? = null
        private set
    var place: DecisionTree<Place>? = null
        private set

    fun init(activities: List<RichActivity>) = synchronized(this) {
        activityType = learnActivityType(activities)
        vehicle = learnVehicle(activities)
        place = learnPlace(activities)
    }

    fun classifyNow(context: Context) = Prediction(
        activityType = activityType?.classifyNow(context),
        vehicle = vehicle?.classifyNow(context),
        place = place?.classifyNow(context),
    )

    private fun learnActivityType(
        activities: List<RichActivity>,
    ): DecisionTree<ActivityType> {
        val examples = Examples(activities.map {
            val attributes =
                attributes(it.activity.startTime.toLocalDateTime(), it.activity.wifiBssid)
            Example(it.type, attributes)
        })

        return DecisionTree.learn(examples)
    }

    private fun learnVehicle(
        activities: List<RichActivity>,
    ): DecisionTree<Vehicle> {
        val examples = Examples(activities.map {
            val attributes =
                attributes(it.activity.startTime.toLocalDateTime(), it.activity.wifiBssid)
            Example(it.activity.vehicle, attributes)
        })

        return DecisionTree.learn(examples)
    }

    private fun learnPlace(activities: List<RichActivity>): DecisionTree<Place> {
        val examples = Examples(activities.map {
            val attributes =
                attributes(it.activity.startTime.toLocalDateTime(), it.activity.wifiBssid)
            Example(it.place, attributes)
        })

        return DecisionTree.learn(examples)
    }

    private fun attributes(date: LocalDateTime, wifiBssid: String?): List<Any?> {
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
        return listOf(timeOfDay, weekDay, wifiBssid)
    }

    private fun <T : Any> DecisionTree<T>.classifyNow(context: Context) =
        classify(attributes(LocalDateTime.now(), context.getCurrentBssid()))
}
