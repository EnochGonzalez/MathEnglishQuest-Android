package com.upchiapas.mathenglishquest.integration.qr

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.floor

/**
 * Dibuja el QR de [content]. Siempre es negro sobre blanco, con su margen, aunque la app esté en modo oscuro
 * (si no, muchos lectores no lo reconocen).
 */
@Composable
fun QrCodeView(
    content: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp
) {
    // Si por algo no se pudo generar, simplemente no se muestra (el código de texto sigue ahí)
    val matrix = remember(content) { runCatching { QrCodeGenerator.generate(content) }.getOrNull() } ?: return
    // Se guarda en otra variable porque dentro de semantics{} el nombre "contentDescription" es también la propiedad
    // de accesibilidad, y Kotlin podría confundirlas
    val description = contentDescription

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(12.dp)
            .semantics { this.contentDescription = description }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val side = minOf(this.size.width, this.size.height)
            val cell = side / matrix.size
            for (y in 0 until matrix.size) {
                for (x in 0 until matrix.size) {
                    if (!matrix[x, y]) continue
                    // Se redondea hacia afuera para que no queden líneas blancas finas entre módulos
                    val left = floor(x * cell)
                    val top = floor(y * cell)
                    val right = ceil((x + 1) * cell)
                    val bottom = ceil((y + 1) * cell)
                    drawRect(
                        color = Color.Black,
                        topLeft = Offset(left, top),
                        size = Size(right - left, bottom - top)
                    )
                }
            }
        }
    }
}
