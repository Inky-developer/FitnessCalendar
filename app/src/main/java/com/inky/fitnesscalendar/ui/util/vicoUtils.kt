package com.inky.fitnesscalendar.ui.util

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.patrykandpatrick.vico.compose.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.compose.common.Fill

fun defaultAreaFill(
    fill: Color,
): LineCartesianLayer.AreaFill {
    val fillColorStart = fill.copy(alpha = 0.5f)
    val gradientFill = Fill(Brush.verticalGradient(listOf(fillColorStart, Color.Transparent)))
    return LineCartesianLayer.AreaFill.single(gradientFill)
}