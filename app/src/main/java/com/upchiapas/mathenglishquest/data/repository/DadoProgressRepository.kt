package com.upchiapas.mathenglishquest.data.repository

import android.content.Context
import com.upchiapas.mathenglishquest.data.model.DadoProgress
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.domain.dado.DadoProgressStore

/**
 * Guarda el progreso del Dado en SharedPreferences, separado por usuario
 * (equivale al localStorage "meq_dado_state_<correo>" del prototipo).
 */
class DadoProgressRepository(context: Context) : DadoProgressStore {
    private val prefs = context.applicationContext.getSharedPreferences("meq_dado", Context.MODE_PRIVATE)

    override fun load(userKey: String): DadoProgress? {
        if (!prefs.contains(key(userKey, HALF_HEARTS))) return null
        return DadoProgress(
            halfHearts = prefs.getInt(key(userKey, HALF_HEARTS), 6),
            streak = prefs.getInt(key(userKey, STREAK), 0),
            maxUnlockedLevel = prefs.getInt(key(userKey, MAX_UNLOCKED), 0),
            currentLevel = prefs.getInt(key(userKey, CURRENT_LEVEL), 0),
            mode = runCatching {
                GameMode.valueOf(prefs.getString(key(userKey, MODE), GameMode.DIRECTO.name) ?: GameMode.DIRECTO.name)
            }.getOrDefault(GameMode.DIRECTO),
            hadProgressedBeyondEasy = prefs.getBoolean(key(userKey, PROGRESSED), false),
            allowNegatives = prefs.getBoolean(key(userKey, NEGATIVES), false)
        )
    }

    override fun save(userKey: String, progress: DadoProgress) {
        prefs.edit()
            .putInt(key(userKey, HALF_HEARTS), progress.halfHearts)
            .putInt(key(userKey, STREAK), progress.streak)
            .putInt(key(userKey, MAX_UNLOCKED), progress.maxUnlockedLevel)
            .putInt(key(userKey, CURRENT_LEVEL), progress.currentLevel)
            .putString(key(userKey, MODE), progress.mode.name)
            .putBoolean(key(userKey, PROGRESSED), progress.hadProgressedBeyondEasy)
            .putBoolean(key(userKey, NEGATIVES), progress.allowNegatives)
            .apply()
    }

    private fun key(userKey: String, name: String) = "$userKey|$name"

    companion object {
        /** Clave para quien entra "sin correo". */
        const val GUEST_KEY = "invitado"

        private const val HALF_HEARTS = "half_hearts"
        private const val STREAK = "streak"
        private const val MAX_UNLOCKED = "max_unlocked"
        private const val CURRENT_LEVEL = "current_level"
        private const val MODE = "mode"
        private const val PROGRESSED = "progressed"
        private const val NEGATIVES = "negatives"
    }
}
