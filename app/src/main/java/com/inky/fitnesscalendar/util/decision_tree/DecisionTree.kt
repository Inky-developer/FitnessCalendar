package com.inky.fitnesscalendar.util.decision_tree

import com.inky.fitnesscalendar.db.entities.ActivityType
import com.inky.fitnesscalendar.db.entities.TypeActivity
import com.inky.fitnesscalendar.util.removedAt
import java.time.Instant
import java.util.Calendar
import java.util.Date

sealed class DecisionTree<Classification> {
    data class Leaf<T>(val value: T?) : DecisionTree<T>()

    data class Node<T>(
        val attributeIndex: Int,
        val children: Map<Any, DecisionTree<T>>,
        val default: T?
    ) : DecisionTree<T>()

    private fun classify(data: List<Any>): Classification? {
        return when (this) {
            is Leaf -> value
            is Node -> {
                val child = children[data[attributeIndex]] ?: return default
                child.classify(data.removedAt(attributeIndex))
            }
        }
    }

    fun classifyNow() = classify(attributes(Date.from(Instant.now())))

    companion object {
        fun attributes(date: Date): List<Any> {
            val calendar = Calendar.getInstance().apply {
                time = date
            }
            val minuteOfDay =
                calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
            // Segments:
            // 0: [2-6) Uhr
            // 1: [6-10) Uhr
            // 2: [10-14) Uhr
            // 3: [14-18) Uhr
            // 4: [18-22) Uhr
            // 5: [22-2) Uhr
            val timeOfDay =
                ((22.0 * 60 + minuteOfDay.toDouble()).mod(24.0 * 60) / (4 * 60)).toInt()
            val weekDay = calendar.get(Calendar.DAY_OF_WEEK)
            return listOf(timeOfDay, weekDay)
        }

        fun learnFromActivities(activities: List<TypeActivity>): DecisionTree<ActivityType> {
            val examples = Examples(activities.map {
                val attributes = attributes(it.activity.startTime)
                Example(it.type, attributes)
            })

            return learn(examples)
        }

        private fun <T> learn(examples: Examples<T>): DecisionTree<T> {
            if (examples.isEmpty()) {
                return Leaf(value = null)
            }

            val first = examples.first()
            if (examples.values.all { it.classification == first.classification }) {
                return Leaf(value = first.classification)
            }

            if (first.attributes.isEmpty()) {
                return Leaf(value = mostCommon(examples.values.map { it.classification }))
            }

            val bestSplitIndex = bestSplit(examples)
            val childTrees =
                examples.splitByAttribute(bestSplitIndex).mapValues { (_, v) -> learn(v) }
            return Node(
                attributeIndex = bestSplitIndex,
                children = childTrees,
                default = mostCommon(examples.values.map { it.classification })
            )
        }

        private fun <T> bestSplit(examples: Examples<T>): Int {
            val rootEntropy = examples.entropy()

            val first = examples.first()
            val splits: MutableList<Double> = mutableListOf()
            for (attributeIndex in 0..<first.attributes.size) {
                val children =
                    examples.groupByAttribute(attributeIndex).map { (_, v) -> Examples(v) }
                val totalEntropy =
                    children.sumOf { it.entropy() * (it.values.size.toDouble() / examples.values.size.toDouble()) }
                val reduction = rootEntropy - totalEntropy
                splits.add(reduction)
            }

            return splits.withIndex().maxBy { it.value }.index
        }

        private fun <T> mostCommon(values: List<T>): T? {
            return values.groupBy { it }.maxByOrNull { it.value.size }?.key
        }
    }
}