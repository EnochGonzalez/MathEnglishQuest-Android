package com.upchiapas.mathenglishquest.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

/** Carga una foto ya guardada, reducida para la miniatura o la vista grande. */
object BitmapLoader {
    fun decodeSampled(file: File, maxSide: Int): Bitmap? {
        if (!file.exists()) return null
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = BitmapFactory.Options().apply {
                inSampleSize = ImageCompressor.sampleSizeFor(bounds.outWidth, bounds.outHeight, maxSide)
            }
            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (_: Exception) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }
}
