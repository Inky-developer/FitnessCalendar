package com.inky.fitnesscalendar.util.prediction.decision_tree

import kotlin.math.log2

data class Examples<T : Any>(val values: List<Example<T>>) {

    fun entropy(): Double {
        val probabilities = itemProbabilities()
        return entropy(probabilities)
    }

    private fun itemProbabilities(): List<Double> {
        return values.groupBy { it.classification }
            .map { it.value.size.toDouble() / values.size.toDouble() }
    }

    fun groupByAttribute(attributeIndex: Int): Map<Any?, List<Example<T>>> {
        return values.groupBy { it.attributes[attributeIndex] }
    }

    fun splitByAttribute(attributeIndex: Int): Map<Any?, Examples<T>> {
        return groupByAttribute(attributeIndex).mapValues { (_, v) ->
            Examples(v.map { it.withoutAttribute(attributeIndex) })
        }
    }

    fun isEmpty() = values.isEmpty()

    fun first() = values.first()

    companion object {
        fun entropy(probabilities: List<Double>) = -probabilities.sumOf { it * log2(it) }
    }
}