package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max

/**
 * Reduce una foto y la guarda como JPG (equivale a compressImageFile del prototipo).
 * Respeta la orientación con la que se tomó la foto, para que no se vea de lado.
 * Si algo falla deja el motivo en Logcat (etiqueta "ImageCompressor").
 */
object ImageCompressor {

    private const val TAG = "ImageCompressor"

    /** Lado más largo en píxeles. 1280 deja el manuscrito legible sin pesar mucho. */
    const val DEFAULT_MAX_SIDE = 1280
    const val DEFAULT_QUALITY = 80

    suspend fun compressToFile(
        context: Context,
        source: Uri,
        destination: File,
        maxSide: Int = DEFAULT_MAX_SIDE,
        quality: Int = DEFAULT_QUALITY
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver

            // 1) Medir sin cargar la foto completa en memoria.
            // OJO: con inJustDecodeBounds, decodeStream SIEMPRE devuelve null (solo llena outWidth y outHeight),
            // así que ese null NO significa error: el éxito se comprueba con el tamaño leído.
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val stream = resolver.openInputStream(source)
            if (stream == null) {
                Log.e(TAG, "No se pudo abrir la foto: $source")
                return@withContext false
            }
            stream.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                Log.e(TAG, "No se pudo leer el tamaño de la foto (formato no soportado o archivo dañado): $source")
                return@withContext false
            }

            // 2) Cargarla ya reducida
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSide)
            }
            var bitmap = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
            if (bitmap == null) {
                Log.e(TAG, "No se pudo decodificar la foto: $source")
                return@withContext false
            }

            // 3) Girar según la orientación guardada por la cámara (si no se puede leer, se deja como está)
            val degrees = readRotation(context, source)

            // 4) Girar y ajustar al tamaño final
            val matrix = Matrix()
            if (degrees != 0) matrix.postRotate(degrees.toFloat())
            val longest = max(bitmap.width, bitmap.height)
            if (longest > maxSide) {
                val scale = maxSide.toFloat() / longest
                matrix.postScale(scale, scale)
            }
            if (!matrix.isIdentity) {
                val adjusted = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                if (adjusted !== bitmap) bitmap.recycle()
                bitmap = adjusted
            }

            // 5) Guardar
            destination.parentFile?.mkdirs()
            val written = FileOutputStream(destination).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            bitmap.recycle()
            if (!written) {
                Log.e(TAG, "No se pudo escribir el JPG en ${destination.path}")
                destination.delete()
            }
            written
        } catch (e: Exception) {
            Log.e(TAG, "Error al comprimir la foto: ${e.javaClass.simpleName}: ${e.message}")
            destination.delete()
            false
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Sin memoria para procesar la foto")
            destination.delete()
            false
        }
    }

    /** Potencia de 2 más grande que deja el lado largo todavía >= [maxSide]. */
    internal fun sampleSizeFor(width: Int, height: Int, maxSide: Int): Int {
        val longest = max(width, height)
        var sample = 1
        while (longest / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    /** La orientación es un extra: si no se puede leer (por ejemplo en un PNG), la foto se guarda sin girar. */
    private fun readRotation(context: Context, source: Uri): Int = try {
        context.contentResolver.openInputStream(source)?.use { stream ->
            when (ExifInterface(stream).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        Log.w(TAG, "No se pudo leer la orientación de la foto: ${e.message}")
        0
    }
}
