package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/** Archivo temporal donde la cámara deja la foto antes de que se comprima y se guarde. */
object EvidenceFiles {
    private const val TEMP_DIR = "evidencias_tmp"

    fun authority(context: Context): String = "${context.packageName}.fileprovider"

    fun createCaptureUri(context: Context): Uri {
        val dir = File(context.cacheDir, TEMP_DIR).apply { mkdirs() }
        val file = File.createTempFile("captura_", ".jpg", dir)
        return FileProvider.getUriForFile(context, authority(context), file)
    }

    /** Borra la foto temporal de la cámara (si la dirección es de nuestro FileProvider). */
    fun deleteTemp(context: Context, uri: Uri) {
        if (uri.authority != authority(context)) return
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (_: Exception) {
            // Si no se puede borrar, Android limpia la caché cuando haga falta
        }
    }
}
