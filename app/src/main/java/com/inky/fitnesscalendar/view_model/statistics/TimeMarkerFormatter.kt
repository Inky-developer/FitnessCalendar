package com.inky.fitnesscalendar.view_model.statistics

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.inky.fitnesscalendar.data.measure.format
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarkerValueFormatter
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import kotlin.time.Duration.Companion.hours

class TimeMarkerFormatter : CartesianMarkerValueFormatter {
    override fun format(
        context: CartesianDrawContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val builder = SpannableStringBuilder()

        for (target in targets) {
            when (target) {
                is ColumnCartesianLayerMarkerTarget -> {
                    val columns = target.columns.filter { it.entry.y > 0 }
                    if (columns.isEmpty()) {
                        continue
                    }
                    val lastIndex = columns.lastIndex
                    val total = columns.sumOf { it.entry.y.toDouble() }
                    val totalTime = total.hours.format()
                    builder.append("$totalTime (")
                    columns.forEachIndexed { index, column ->
                        builder.append(
                            column.entry.y.toDouble().hours.format(),
                            ForegroundColorSpan(column.color),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        if (index != lastIndex) {
                            builder.append(", ")
                        }
                    }
                    builder.append(")")
                }

                else -> throw NotImplementedError("TimeMarkerFormatter only supports columns")
            }
        }

        return builder
    }
}