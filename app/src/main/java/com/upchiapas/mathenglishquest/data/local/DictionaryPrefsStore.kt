package com.upchiapas.mathenglishquest.data.local

import android.content.Context
import com.google.gson.Gson
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryLocalStore
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry

/**
 * Guarda en el celular (SharedPreferences) el historial de palabras y la última respuesta de cada una,
 * para poder verlas sin internet y que sigan ahí al cerrar y abrir la app.
 */
class DictionaryPrefsStore(
    context: Context,
    private val gson: Gson = Gson()
) : DictionaryLocalStore {

    private val prefs = context.applicationContext.getSharedPreferences("meq_dictionary", Context.MODE_PRIVATE)

    override fun loadHistory(): List<String> = try {
        gson.fromJson(prefs.getString(KEY_HISTORY, null), Array<String>::class.java)?.toList().orEmpty()
    } catch (_: Exception) {
        emptyList()
    }

    override fun saveHistory(words: List<String>) {
        prefs.edit().putString(KEY_HISTORY, gson.toJson(words)).apply()
    }

    override fun loadEntry(word: String): WordEntry? = try {
        gson.fromJson(prefs.getString(entryKey(word), null), WordEntry::class.java)
    } catch (_: Exception) {
        null
    }

    override fun saveEntry(word: String, entry: WordEntry) {
        prefs.edit().putString(entryKey(word), gson.toJson(entry)).apply()
    }

    override fun removeEntry(word: String) {
        prefs.edit().remove(entryKey(word)).apply()
    }

    private fun entryKey(word: String) = "entry|$word"

    private companion object {
        const val KEY_HISTORY = "history"
    }
}
