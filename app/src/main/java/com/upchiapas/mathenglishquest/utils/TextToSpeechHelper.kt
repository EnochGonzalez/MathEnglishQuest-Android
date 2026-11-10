package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.upchiapas.mathenglishquest.di.AppContainer
import java.util.Locale

/**
 * Voz del celular en inglés. Hay UNA sola para toda la app: arranca cuando se abre la app (durante la pantalla
 * de carga), así el primer toque de 🔊 en cualquier módulo suena casi al instante.
 *
 * - Si se pide hablar antes de que el motor esté listo, la frase espera y suena en cuanto termine de arrancar.
 * - Si el motor no arrancó o falta la voz en inglés, se avisa a la persona y se vuelve a intentar en el siguiente
 *   toque (antes quedaba mudo toda la sesión sin decir nada).
 */
class TextToSpeechHelper(context: Context) {
    private val appContext = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isReady = false
    private var initFailed = false
    private var pendingText: String? = null

    init {
        start()
    }

    private fun start() {
        isReady = false
        initFailed = false
        tts = TextToSpeech(appContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                Log.w(TAG, "No arrancó el motor de voz (status=$status)")
                fail("No se pudo iniciar la voz del celular. Revisa Ajustes > Texto a voz.")
                return@TextToSpeech
            }
            val engine = tts ?: return@TextToSpeech
            var result = engine.setLanguage(Locale.US)
            if (isMissing(result)) result = engine.setLanguage(Locale.ENGLISH)   // por si solo hay otra variante del inglés
            if (isMissing(result)) {
                Log.w(TAG, "El celular no tiene la voz en inglés instalada (result=$result)")
                fail("Falta la voz en inglés. Instálala en Ajustes > Texto a voz.")
                return@TextToSpeech
            }
            isReady = true
            pendingText?.let { speakNow(it) }
            pendingText = null
        }
    }

    fun speak(text: String) {
        when {
            isReady -> speakNow(text)
            initFailed -> {
                // El arranque anterior falló: se intenta de nuevo con este toque (por ejemplo, si ya instalaron la voz)
                pendingText = text
                tts?.shutdown()
                start()
            }
            else -> pendingText = text          // todavía arrancando: suena en cuanto esté listo
        }
    }

    private fun speakNow(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_$text")
    }

    private fun isMissing(result: Int?) =
        result == null || result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED

    private fun fail(message: String) {
        initFailed = true
        Toast.makeText(appContext, message, Toast.LENGTH_LONG).show()
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
