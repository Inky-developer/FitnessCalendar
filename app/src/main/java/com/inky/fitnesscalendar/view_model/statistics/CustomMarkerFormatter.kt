package com.inky.fitnesscalendar.view_model.statistics

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import com.patrykandpatrick.vico.compose.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.compose.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.DefaultCartesianMarker
import com.patrykandpatrick.vico.compose.cartesian.marker.LineCartesianLayerMarkerTarget

/**
 * Custom marker formatter that is a bit more versatile than vicos default marker formatter.
 * It additionally supports displaying durations through the more generic formatting function and
 * filtering out values
 */
class CustomMarkerFormatter(val formatValue: (Double) -> String) :
    DefaultCartesianMarker.ValueFormatter {
    private fun AnnotatedString.Builder.append(text: String, color: Color) {
        withStyle(SpanStyle(color = color)) {
            append(text)
        }
    }

    override fun format(
        context: CartesianDrawingContext,
        targets: List<CartesianMarker.Target>
    ): CharSequence {
        val builder = AnnotatedString.Builder()

        for (target in targets) {
            when (target) {
                is LineCartesianLayerMarkerTarget -> {
                    val columns = target.points.filter { it.entry.y > 0 }
                    if (columns.isEmpty()) {
                        continue
                    }
                    val lastIndex = columns.lastIndex
                    builder.append("(")
                    columns.forEachIndexed { index, column ->
                        builder.append(formatValue(column.entry.y), column.color)
                        if (index != lastIndex) {
                            builder.append(", ")
                        }
                    }
                    builder.append(")")
                }

                else -> throw NotImplementedError("TimeMarkerFormatter only supports lines")
            }
        }

        return builder.toAnnotatedString()
    }
}