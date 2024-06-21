package com.inky.fitnesscalendar.view_model.statistics

import com.patrykandpatrick.vico.core.cartesian.CartesianDrawContext
import com.patrykandpatrick.vico.core.cartesian.marker.CartesianMarker
import com.patrykandpatrick.vico.core.cartesian.marker.ColumnCartesianLayerMarkerTarget
import com.patrykandpatrick.vico.core.common.component.TextComponent

class IconMarker(private val indicator: TextComponent, private val emojis: List<String>) :
    CartesianMarker {
    override fun draw(context: CartesianDrawContext, targets: List<CartesianMarker.Target>) {
        // Don't draw markers if it gets too crowded
        if (context.zoom < 0.3f) {
            return
        }

        val indicatorHeight = indicator.textSizeSp

        with(context) {
            val bounds = context.chartBounds
            var bottomEdge = bounds.bottom
            targets.forEach { target ->
                when (target) {
                    is ColumnCartesianLayerMarkerTarget -> {
                        target.columns.zip(emojis).forEach { (column, emoji) ->
                            val columnHeight = bottomEdge - column.canvasY
                            if (columnHeight > indicatorHeight) {
                                val posY = (column.canvasY + bottomEdge) / 2f
                                drawIndicator(target.canvasX, posY, emoji)
                                bottomEdge = column.canvasY
                            }
                        }
                    }

                    else -> throw NotImplementedError("IconMarker only supports columns")
                }
            }
        }
    }

    private fun CartesianDrawContext.drawIndicator(x: Float, y: Float, emoji: String) {
        indicator.drawText(
            this,
            emoji,
            x,
            y
        )
    }
}