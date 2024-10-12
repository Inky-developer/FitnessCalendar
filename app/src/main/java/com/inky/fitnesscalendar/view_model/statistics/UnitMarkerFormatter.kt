package com.inky.fitnesscalendar.view_model.statistics

import android.text.SpannableStringBuilder
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import java.text.DecimalFormat

/**
 * Similar to [DefaultCartesianMarkerValueFormatter], but hides elements if their value is 0 and
 * provides a constructor for rendering the value with a unit additionally
 */
class UnitMarkerFormatter(decimalFormat: DecimalFormat = DecimalFormat("#.#;-#.#")) :
    DefaultCartesianMarkerValueFormatter(decimalFormat) {

    constructor(unit: String) : this(DecimalFormat("#.# $unit;-#.# $unit"))

    override fun SpannableStringBuilder.append(target: CartesianMarker.Target, shorten: Boolean) {
        if (target !is LineCartesianLayerMarkerTarget) {
            throw AssertionError("Unsupported CartesianLayerMarkerTarget")
        }

        val points = target.points.filter { it.entry.y > 0 }
        points.forEachIndexed { index, point ->
            if (point.entry.y > 0) {
                append(point.entry.y, point.color)
                if (index != points.lastIndex) append(", ")
            }
        }
    }
}