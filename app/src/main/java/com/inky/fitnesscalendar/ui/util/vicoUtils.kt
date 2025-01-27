package com.inky.fitnesscalendar.ui.util

import android.graphics.Color
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Fill
import com.patrykandpatrick.vico.core.common.shader.ShaderProvider

fun defaultAreaFill(
    fill: Color,
): LineCartesianLayer.AreaFill {
    val fillColorStart = Color.valueOf(fill.red(), fill.green(), fill.blue(), 0.5f).toArgb()
    val gradientFill =
        Fill(ShaderProvider.verticalGradient(fillColorStart, Color.TRANSPARENT))
    return LineCartesianLayer.AreaFill.single(gradientFill)
}