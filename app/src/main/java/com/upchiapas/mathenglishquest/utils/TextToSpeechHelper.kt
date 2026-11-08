package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.upchiapas.mathenglishquest.di.AppContainer
import java.util.Locale

/**
 * Voz del celular en inglés. Hay UNA sola para toda la app: arranca cuando se abre la app (durante la pantalla
 * de carga), así el primer toque de 🔊 en cualquier módulo suena casi al instante.
 *
 * Si se pide hablar antes de que el motor esté listo, la frase queda esperando y suena en cuanto termine de arrancar
 * (antes esos toques se perdían en silencio).
 */
class TextToSpeechHelper(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var pendingText: String? = null

    init {
        start()
    }

    private fun start() {
        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.w(TAG, "No arrancó el motor de voz (status=$status)")
                return@TextToSpeech
            }
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "El celular no tiene la voz en inglés instalada")
                return@TextToSpeech
            }
            isReady = true
            pendingText?.let { speakNow(it) }
            pendingText = null
        }
    }

    fun speak(text: String) {
        if (isReady) {
            speakNow(text)
        } else {
            pendingText = text          // suena en cuanto el motor esté listo
            if (tts == null) start()
        }
    }

    private fun speakNow(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_$text")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }

    private companion object {
        const val TAG = "TextToSpeech"
    }
}

/** La voz compartida de la app. Ya no se destruye al salir de cada pantalla. */
@Composable
fun rememberTextToSpeechHelper(): TextToSpeechHelper {
    val context = LocalContext.current.applicationContext
    return remember { AppContainer.getInstance(context).textToSpeech }
}
