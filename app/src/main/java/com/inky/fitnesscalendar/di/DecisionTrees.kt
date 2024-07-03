package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.data.Vehicle
import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.TypeActivity
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree

object DecisionTrees {
    var activityType: DecisionTree<ActivityType>? = null
        private set

    var vehicle: DecisionTree<Vehicle>? = null
        private set

    fun init(activities: List<TypeActivity>) = synchronized(this) {
        activityType = DecisionTree.learnActivityType(activities)
        vehicle = DecisionTree.learnVehicle(activities)
    }
}