package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import com.upchiapas.mathenglishquest.utils.BitmapLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File

/** Cuántas veces se reintenta leer la foto mientras se descarga (≈24 s en total). */
private const val RETRIES = 30
private const val RETRY_DELAY_MS = 800L

/** Muestra una foto guardada. La carga se hace fuera del hilo principal para que la lista no se trabe. */
@Composable
fun EvidenceImage(
    file: File,
    modifier: Modifier = Modifier,
    maxSidePx: Int = 400,
    contentScale: ContentScale = ContentScale.Crop
) {
    // El tamaño y la fecha cambian cuando la foto llega de la nube, así que sirven como
    // "clave". Además se reintenta la lectura unos segundos: la descarga termina después
    // de que la tarjeta ya se dibujó y la lista no cambia, así que no habría recomposición.
    val version = file.length() to file.lastModified()

    val bitmap by produceState<ImageBitmap?>(initialValue = null, file, maxSidePx, version) {
        repeat(RETRIES) { intento ->
            val decoded = withContext(Dispatchers.IO) {
                if (file.exists()) BitmapLoader.decodeSampled(file, maxSidePx)?.asImageBitmap() else null
            }
            if (decoded != null) {
                value = decoded
                return@produceState
            }
            if (intento < RETRIES - 1) delay(RETRY_DELAY_MS)
        }
    }

    Box(
        modifier = modifier.background(AppTheme.colors.line),
        contentAlignment = Alignment.Center
    ) {
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Evidencia",
                contentScale = contentScale,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
