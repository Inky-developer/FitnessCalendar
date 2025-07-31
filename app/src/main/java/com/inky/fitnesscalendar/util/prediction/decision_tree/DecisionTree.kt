package com.inky.fitnesscalendar.util.prediction.decision_tree

import com.inky.fitnesscalendar.util.removedAt

sealed class DecisionTree<Classification : Any> {
    data class Leaf<T : Any>(val value: T?) : DecisionTree<T>()

    data class Node<T : Any>(
        val attributeIndex: Int,
        val children: Map<Any?, DecisionTree<T>>,
        val default: T?
    ) : DecisionTree<T>()

    fun classify(data: List<Any?>): Classification? {
        return when (this) {
            is Leaf -> value
            is Node -> {
                val child = children[data[attributeIndex]] ?: return default
                child.classify(data.removedAt(attributeIndex))
            }
        }
    }

    companion object {
        fun <T : Any> learn(examples: Examples<T>): DecisionTree<T> {
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

        private fun <T : Any> bestSplit(examples: Examples<T>): Int {
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