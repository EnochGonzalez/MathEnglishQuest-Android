package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

class TextToSpeechHelper(private val context: Context) {
    private var tts: TextToSpeech? = null
    private var isReady = false

    fun speak(text: String) {
        if (tts == null) {
            tts = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        isReady = true
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_$text")
                    }
                }
            }
        } else if (isReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID_$text")
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isReady = false
    }
}

@Composable
fun rememberTextToSpeechHelper(): TextToSpeechHelper {
    val context = LocalContext.current
    val helper = remember { TextToSpeechHelper(context) }
    DisposableEffect(helper) {
        onDispose {
            helper.shutdown()
        }
    }
    return helper
}
