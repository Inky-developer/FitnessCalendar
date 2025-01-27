package com.inky.fitnesscalendar.view_model.statistics

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import java.text.DecimalFormat

/**
 * Similar to [DefaultCartesianMarker.ValueFormatter.default], but hides elements if their value is 0 and
 * provides a constructor for rendering the value with a unit additionally
 */
class UnitMarkerFormatter(decimalFormat: DecimalFormat = DecimalFormat("#.#;-#.#")) :
    DefaultCartesianMarker.ValueFormatter {

    constructor(unit: String) : this(DecimalFormat("#.# $unit;-#.# $unit"))

    private val delegate = DefaultCartesianMarker.ValueFormatter.default(decimalFormat)

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val filteredTargets: List<CartesianMarker.Target> = targets.map { target ->
            if (target !is LineCartesianLayerMarkerTarget) return@map target

            object : LineCartesianLayerMarkerTarget by target {
                private val _filteredPoints = target.points.filter { it.entry.y > 0 }

                override val points get() = _filteredPoints
            }
        }

        return delegate.format(context, filteredTargets)
    }
}