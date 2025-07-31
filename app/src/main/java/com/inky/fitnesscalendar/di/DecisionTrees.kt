package com.inky.fitnesscalendar.di

import android.os.Parcelable
import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.Place
import com.inky.fitnesscalendar.db.entities.RichActivity
import com.inky.fitnesscalendar.util.prediction.Predictor
import com.inky.fitnesscalendar.util.prediction.decision_tree.DecisionTree
import com.inky.fitnesscalendar.util.prediction.decision_tree.Example
import com.inky.fitnesscalendar.util.prediction.decision_tree.Examples
import com.inky.fitnesscalendar.util.toLocalDateTime
import kotlinx.parcelize.Parcelize
import java.time.DayOfWeek
import java.time.LocalDateTime

object DecisionTrees {
    @Parcelize
    data class PredictionResult(
        val activityType: ActivityType?,
        val vehicle: Vehicle?,
        val place: Place?
    ) : Parcelable

    data class DefaultInputs(val timeOfDay: Int, val weekDay: DayOfWeek) : Predictor.Inputs {
        override fun asList() = listOf(timeOfDay, weekDay)

        companion object {
            fun fromLocalDateTime(date: LocalDateTime): DefaultInputs {
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
                return DefaultInputs(timeOfDay, weekDay)
            }
        }
    }

    data class ActivityTypeInputs(
        val defaultInputs: DefaultInputs,
        val activityType: ActivityType?
    ) : Predictor.Inputs {
        override fun asList() = defaultInputs.asList() + activityType

        companion object {
            fun fromLocalDateTime(date: LocalDateTime, activityType: ActivityType) =
                ActivityTypeInputs(DefaultInputs.fromLocalDateTime(date), activityType)
        }
    }

    class ActivityTypePredictor(val tree: DecisionTree<ActivityType>) :
        Predictor<DefaultInputs, ActivityType?> {
        override fun predict(inputs: DefaultInputs) = tree.classify(inputs.asList())

        companion object {
            fun learn(activities: List<RichActivity>): ActivityTypePredictor {
                val examples = Examples(activities.map {
                    val attributes =
                        DefaultInputs.fromLocalDateTime(it.activity.startTime.toLocalDateTime())
                    Example(it.type, attributes.asList())
                })

                return ActivityTypePredictor(DecisionTree.learn(examples))
            }
        }
    }

    class VehiclePredictor(val tree: DecisionTree<Vehicle>) :
        Predictor<ActivityTypeInputs, Vehicle?> {
        override fun predict(inputs: ActivityTypeInputs) = tree.classify(inputs.asList())

        companion object {
            fun learn(activities: List<RichActivity>): VehiclePredictor {
                val examples = Examples(activities.map {
                    val attributes = ActivityTypeInputs.fromLocalDateTime(
                        it.activity.startTime.toLocalDateTime(),
                        it.type
                    )
                    Example(it.activity.vehicle, attributes.asList())
                })

                return VehiclePredictor(DecisionTree.learn(examples))
            }
        }
    }

    class PlacePredictor(val tree: DecisionTree<Place>) : Predictor<ActivityTypeInputs, Place?> {
        override fun predict(inputs: ActivityTypeInputs) = tree.classify(inputs.asList())

        companion object {
            fun learn(activities: List<RichActivity>): PlacePredictor {
                val examples = Examples(activities.map {
                    val attributes = ActivityTypeInputs.fromLocalDateTime(
                        it.activity.startTime.toLocalDateTime(),
                        it.type
                    )
                    Example(it.place, attributes.asList())
                })

                return PlacePredictor(DecisionTree.learn(examples))
            }
        }
    }

    lateinit var activityTypePredictor: ActivityTypePredictor
    lateinit var vehiclePredictor: VehiclePredictor
    lateinit var placePredictor: PlacePredictor

    fun init(activities: List<RichActivity>) = synchronized(this) {
        activityTypePredictor = ActivityTypePredictor.learn(activities)
        vehiclePredictor = VehiclePredictor.learn(activities)
        placePredictor = PlacePredictor.learn(activities)
    }

    fun classifyNow(selectedActivityType: ActivityType?): PredictionResult {
        val defaultInputs = DefaultInputs.fromLocalDateTime(LocalDateTime.now())
        val predictedActivityType = activityTypePredictor.predict(defaultInputs)
        val activityTypeForPrediction = selectedActivityType ?: predictedActivityType
        val activityTypeInputs = ActivityTypeInputs(defaultInputs, activityTypeForPrediction)
        return PredictionResult(
            activityType = predictedActivityType,
            vehicle = vehiclePredictor.predict(activityTypeInputs),
            place = placePredictor.predict(activityTypeInputs),
        )
    }
}
