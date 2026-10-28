package com.upchiapas.mathenglishquest.utils

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import java.util.Locale

@Composable
fun rememberVoiceRecognizerLauncher(
    onSpeechResult: (String) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    if (result.resultCode == Activity.RESULT_OK) {
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
        if (spokenText != null && spokenText.isNotBlank()) {
            onSpeechResult(spokenText)
        }
    }
}

fun getVoiceRecognizerIntent(): Intent {
    return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString())
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Responde en voz alta (en inglés)")
    }
}
