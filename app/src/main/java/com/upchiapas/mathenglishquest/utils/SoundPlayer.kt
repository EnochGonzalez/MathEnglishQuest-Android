package com.upchiapas.mathenglishquest.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import androidx.annotation.RawRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val TAG = "SoundPlayer"

/**
 * Reproduce un sonido corto de res/raw (por ejemplo el de inicio).
 * Si [respectSilentMode] es true, no suena con el celular en silencio o en vibración.
 * Cada paso deja un mensaje en Logcat (filtra por la etiqueta "SoundPlayer").
 */
class SoundPlayer(
    private val context: Context,
    private val respectSilentMode: Boolean = true
) {
    private var player: MediaPlayer? = null

    fun play(@RawRes resId: Int) {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val volume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        Log.d(TAG, "play(): ringerMode=${audio.ringerMode} (2 = normal), volumen multimedia=$volume")

        if (respectSilentMode && audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) {
            Log.w(TAG, "No suena: el celular está en silencio o vibración")
            return
        }
        if (volume == 0) {
            Log.w(TAG, "El volumen multimedia está en 0")
        }

        release()
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        val mp = MediaPlayer.create(context, resId, attrs, AudioManager.AUDIO_SESSION_ID_GENERATE)
        if (mp == null) {
            Log.e(TAG, "MediaPlayer.create devolvió null: no se pudo abrir el recurso $resId")
            return
        }
        mp.setOnCompletionListener {
            Log.d(TAG, "Terminó de sonar")
            release()
        }
        mp.setOnErrorListener { _, what, extra ->
            Log.e(TAG, "Error de reproducción: what=$what extra=$extra")
            release()
            true
        }
        player = mp
        mp.start()
        Log.d(TAG, "Reproduciendo (${mp.duration} ms)")
    }

    fun release() {
        player?.let {
            try {
                it.release()
            } catch (e: Exception) {
                Log.w(TAG, "Error al liberar: ${e.message}")
            }
        }
        player = null
    }
}

@Composable
fun rememberSoundPlayer(): SoundPlayer {
    val context = LocalContext.current
    val soundPlayer = remember { SoundPlayer(context) }
    DisposableEffect(soundPlayer) {
        onDispose { soundPlayer.release() }
    }
    return soundPlayer
}