package com.upchiapas.mathenglishquest.data.remote.translation

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.upchiapas.mathenglishquest.data.remote.awaitTask
import com.upchiapas.mathenglishquest.domain.dictionary.TextTranslator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Traduce del inglés al español con ML Kit, dentro del celular (sin servidor y sin límite de uso).
 * La primera vez baja el idioma (unos 30 MB) y después funciona sin internet.
 */
class MlKitTextTranslator : TextTranslator {

    private val translator = Translation.getClient(
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
    )

    private val lock = Mutex()
    @Volatile private var modelReady = false

    override suspend fun prepare(): Boolean {
        if (modelReady) return true
        // Un solo candado: si dos pantallas lo piden a la vez, el idioma se descarga una sola vez
        return lock.withLock {
            if (modelReady) return@withLock true
            try {
                translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).awaitTask()
                modelReady = true
                Log.d(TAG, "Idioma inglés→español listo")
                true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "No se pudo preparar el traductor: ${e.javaClass.simpleName}: ${e.message}")
                false
            }
        }
    }

    override suspend fun translate(text: String): String? {
        if (text.isBlank()) return text
        return try {
            translator.translate(text).awaitTask().takeIf { it.isNotBlank() }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "No se pudo traducir: ${e.javaClass.simpleName}: ${e.message}")
            null
        }
    }

    private companion object {
        const val TAG = "MlKitTranslator"
    }
}
