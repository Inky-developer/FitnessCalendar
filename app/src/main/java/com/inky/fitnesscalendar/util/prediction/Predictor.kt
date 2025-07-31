package com.inky.fitnesscalendar.util.prediction

interface Predictor<I : Predictor.Inputs, O> {
    fun predict(inputs: I): O

    interface Inputs {
        fun asList(): List<Any?>
    }
}