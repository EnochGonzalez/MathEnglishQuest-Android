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
import kotlinx.coroutines.withContext
import java.io.File

/** Muestra una foto guardada. La carga se hace fuera del hilo principal para que la lista no se trabe. */
@Composable
fun EvidenceImage(
    file: File,
    modifier: Modifier = Modifier,
    maxSidePx: Int = 400,
    contentScale: ContentScale = ContentScale.Crop
) {
    val bitmap by produceState<ImageBitmap?>(initialValue = null, file, maxSidePx) {
        value = withContext(Dispatchers.IO) { BitmapLoader.decodeSampled(file, maxSidePx)?.asImageBitmap() }
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
