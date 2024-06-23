package com.inky.fitnesscalendar.di

import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.util.decision_tree.DecisionTree

object ActivityTypeDecisionTree {
    var decisionTree: DecisionTree<ActivityType>? = null
}