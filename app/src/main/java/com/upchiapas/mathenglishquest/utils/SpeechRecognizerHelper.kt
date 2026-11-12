package com.upchiapas.mathenglishquest.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.util.Locale

/** Cuántas interpretaciones alternativas se le piden al reconocedor de voz. */
private const val MAX_VOICE_RESULTS = 5

/**
 * Entrega TODAS las interpretaciones que devuelve el reconocedor (la más probable primero).
 * Con palabras sueltas como "eight" la primera opción suele venir mal ("ate", "8", "hate"...)
 * y la correcta aparece entre las alternativas, por eso no basta con tomar solo la primera.
 */
@Composable
fun rememberVoiceRecognizerLauncher(
    onSpeechResult: (List<String>) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val candidates = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (candidates.isNotEmpty()) {
            onSpeechResult(candidates)
        }
    }
}

fun getVoiceRecognizerIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, MAX_VOICE_RESULTS)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Responde en voz alta (en inglés)")
    }
}
