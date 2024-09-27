package com.inky.fitnesscalendar.view_model.statistics

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.inky.fitnesscalendar.data.measure.format
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.LineCartesianLayerMarkerTarget
import kotlin.time.Duration.Companion.hours

class TimeMarkerFormatter : CartesianMarkerValueFormatter {
    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val builder = SpannableStringBuilder()

        for (target in targets) {
            when (target) {
                is LineCartesianLayerMarkerTarget -> {
                    val columns = target.points.filter { it.entry.y > 0 }
                    if (columns.isEmpty()) {
                        continue
                    }
                    val lastIndex = columns.lastIndex
                    val total = columns.sumOf { it.entry.y }
                    val totalTime = total.hours.format()
                    builder.append("$totalTime (")
                    columns.forEachIndexed { index, column ->
                        builder.append(
                            column.entry.y.hours.format(),
                            ForegroundColorSpan(column.color),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        if (index != lastIndex) {
                            builder.append(", ")
                        }
                    }
                    builder.append(")")
                }

                else -> throw NotImplementedError("TimeMarkerFormatter only supports lines")
            }
        }

        return builder
    }
}