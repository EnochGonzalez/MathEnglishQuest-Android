package com.upchiapas.mathenglishquest.data.local

import android.content.Context

/**
 * Guarda quién tiene la sesión abierta: una cuenta (id de Room) o un invitado sin correo.
 * (Equivale al localStorage "meq_session" del prototipo web.)
 */
class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("meq_session", Context.MODE_PRIVATE)

    fun saveUser(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).putBoolean(KEY_GUEST, false).apply()
    }

    fun saveGuest() {
        prefs.edit().remove(KEY_USER_ID).putBoolean(KEY_GUEST, true).apply()
    }

    fun isGuest(): Boolean = prefs.getBoolean(KEY_GUEST, false)

    /** Id de la cuenta con sesión abierta, o null si no hay (o si es invitado). */
    fun userId(): Int? = if (prefs.contains(KEY_USER_ID)) prefs.getInt(KEY_USER_ID, -1) else null

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_USER_ID = "user_id"
        const val KEY_GUEST = "guest"
    }
}
