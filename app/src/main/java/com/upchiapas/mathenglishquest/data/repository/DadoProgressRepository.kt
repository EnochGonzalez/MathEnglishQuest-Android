package com.upchiapas.mathenglishquest.data.repository

import android.content.Context
import android.util.Log
import com.upchiapas.mathenglishquest.data.model.DadoProgress
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.data.remote.FirebaseAuthDataSource
import com.upchiapas.mathenglishquest.data.remote.FirestoreDataSource
import com.upchiapas.mathenglishquest.data.remote.model.ProgresoDoc
import com.upchiapas.mathenglishquest.domain.dado.DadoProgressStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Progreso del Dado, separado por usuario.
 *
 * Se guarda en SharedPreferences (para que funcione sin internet) **y** en Firestore
 * (`progreso/{uid}`), para que el estudiante lo recupere al cambiar de celular.
 *
 * Si los dos lados no coinciden, gana el más reciente según la fecha de actualización:
 * así no se pierde lo que se jugó sin conexión.
 */
class DadoProgressRepository(
    context: Context,
    private val firebaseAuth: FirebaseAuthDataSource,
    private val firestore: FirestoreDataSource,
    private val scope: CoroutineScope
) : DadoProgressStore {

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
        val ahora = System.currentTimeMillis()
        writePrefs(userKey, progress, ahora)

        // Invitados (sin cuenta) se quedan solo en el teléfono.
        val uid = firebaseAuth.uidActual ?: return
        scope.launch {
            runCatching { firestore.guardarProgreso(uid, progress.toDoc()) }
                .onFailure { Log.w(TAG, "No se pudo guardar el progreso en la nube: ${it.message}") }
        }
    }

    /**
     * Compara la nube con lo local y deja el más reciente. Se llama al abrir el módulo.
     */
    override suspend fun sync(userKey: String) {
        val uid = firebaseAuth.uidActual ?: return
        val remoto = runCatching { firestore.buscarProgreso(uid) }.getOrNull() ?: return

        val localActualizado = prefs.getLong(key(userKey, UPDATED_AT), 0L)
        val remotoActualizado = remoto.actualizadoEn?.toDate()?.time ?: 0L

        when {
            remotoActualizado > localActualizado -> writePrefs(userKey, remoto.toProgress(), remotoActualizado)
            localActualizado > remotoActualizado -> {
                load(userKey)?.let { local ->
                    runCatching { firestore.guardarProgreso(uid, local.toDoc()) }
                }
            }
        }
    }

    private fun writePrefs(userKey: String, progress: DadoProgress, actualizadoEn: Long) {
        prefs.edit()
            .putInt(key(userKey, HALF_HEARTS), progress.halfHearts)
            .putInt(key(userKey, STREAK), progress.streak)
            .putInt(key(userKey, MAX_UNLOCKED), progress.maxUnlockedLevel)
            .putInt(key(userKey, CURRENT_LEVEL), progress.currentLevel)
            .putString(key(userKey, MODE), progress.mode.name)
            .putBoolean(key(userKey, PROGRESSED), progress.hadProgressedBeyondEasy)
            .putBoolean(key(userKey, NEGATIVES), progress.allowNegatives)
            .putLong(key(userKey, UPDATED_AT), actualizadoEn)
            .apply()
    }

    private fun DadoProgress.toDoc() = ProgresoDoc(
        halfHearts = halfHearts,
        streak = streak,
        maxUnlockedLevel = maxUnlockedLevel,
        currentLevel = currentLevel,
        mode = mode.name,
        hadProgressedBeyondEasy = hadProgressedBeyondEasy,
        allowNegatives = allowNegatives
    )

    private fun ProgresoDoc.toProgress() = DadoProgress(
        halfHearts = halfHearts,
        streak = streak,
        maxUnlockedLevel = maxUnlockedLevel,
        currentLevel = currentLevel,
        mode = runCatching { GameMode.valueOf(mode) }.getOrDefault(GameMode.DIRECTO),
        hadProgressedBeyondEasy = hadProgressedBeyondEasy,
        allowNegatives = allowNegatives
    )

    private fun key(userKey: String, name: String) = "$userKey|$name"

    companion object {
        /** Clave para quien entra "sin correo". */
        const val GUEST_KEY = "invitado"

        private const val TAG = "DadoProgressRepository"
        private const val HALF_HEARTS = "half_hearts"
        private const val STREAK = "streak"
        private const val MAX_UNLOCKED = "max_unlocked"
        private const val CURRENT_LEVEL = "current_level"
        private const val MODE = "mode"
        private const val PROGRESSED = "progressed"
        private const val NEGATIVES = "negatives"
        private const val UPDATED_AT = "updated_at"
    }
}
