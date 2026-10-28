package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Dado morado con 5 puntos (".dice-logo" de la pantalla de carga del prototipo).
 * Las medidas salen de un dado de 64px: borde 4, radio 16, puntos de 10 de diámetro.
 */
@Composable
fun DiceLogo(
    modifier: Modifier = Modifier,
    logoSize: Dp = 64.dp,
    color: Color = Color(0xFF8B5CF6)
) {
    Canvas(modifier = modifier.size(logoSize)) {
        val side = this.size.width
        val stroke = side * 4f / 64f
        val radius = side * 16f / 64f

        drawRoundRect(
            color = color,
            topLeft = Offset(stroke / 2f, stroke / 2f),
            size = Size(side - stroke, side - stroke),
            cornerRadius = CornerRadius(radius - stroke / 2f),
            style = Stroke(width = stroke)
        )

        // Cuadrícula 3x3 interior: 40 de 64 (borde 4 + padding 8 a cada lado)
        val inner = side * 40f / 64f
        val origin = (side - inner) / 2f
        val cell = inner / 3f
        val dotRadius = side * 5f / 64f

        listOf(0 to 0, 2 to 0, 1 to 1, 0 to 2, 2 to 2).forEach { (col, row) ->
            drawCircle(
                color = color,
                radius = dotRadius,
                center = Offset(origin + cell * (col + 0.5f), origin + cell * (row + 0.5f))
            )
        }
    }
}
