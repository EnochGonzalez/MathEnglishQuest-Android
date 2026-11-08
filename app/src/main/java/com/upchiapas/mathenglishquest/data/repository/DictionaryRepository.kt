package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryLocalStore
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryRemote
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Único punto por el que la app consulta el diccionario: valida la palabra, la pide a la API externa
 * y guarda en el celular lo que sirvió, para poder verlo después sin internet.
 */
class DictionaryRepository(
    private val remote: DictionaryRemote,
    private val store: DictionaryLocalStore
) {
    private val _history = MutableStateFlow(store.loadHistory())

    /** Palabras buscadas con éxito, la más reciente primero. */
    val history: StateFlow<List<String>> = _history.asStateFlow()

    suspend fun lookup(rawWord: String): LookupResult {
        val word = normalize(rawWord) ?: return LookupResult.InvalidInput

        return when (val result = remote.lookup(word)) {
            is RemoteLookup.Found -> {
                store.saveEntry(word, result.entry)
                addToHistory(word)
                LookupResult.Success(result.entry, fromCache = false)
            }
            RemoteLookup.NotFound -> LookupResult.NotFound
            // Sin internet o con el servidor caído: si ya se había buscado antes, se muestra lo guardado
            RemoteLookup.NoConnection ->
                store.loadEntry(word)?.let { LookupResult.Success(it, fromCache = true) } ?: LookupResult.NoConnection
            is RemoteLookup.ServerError ->
                store.loadEntry(word)?.let { LookupResult.Success(it, fromCache = true) }
                    ?: LookupResult.ServerError(result.code)
        }
    }

    fun removeFromHistory(word: String) {
        store.removeEntry(word)
        updateHistory(_history.value.filter { it != word })
    }

    fun clearHistory() {
        _history.value.forEach(store::removeEntry)
        updateHistory(emptyList())
    }

    private fun addToHistory(word: String) {
        updateHistory((listOf(word) + _history.value.filter { it != word }).take(MAX_HISTORY))
    }

    private fun updateHistory(words: List<String>) {
        _history.value = words
        store.saveHistory(words)
    }

    /** Minúsculas y sin espacios de más. Null si no parece una palabra en inglés (solo letras, guion o apóstrofo). */
    private fun normalize(raw: String): String? {
        val word = raw.trim().lowercase().replace(Regex("\\s+"), " ")
        if (word.isEmpty() || word.length > MAX_LENGTH) return null
        return word.takeIf { VALID.matches(it) }
    }

    private companion object {
        const val MAX_HISTORY = 20
        const val MAX_LENGTH = 40
        val VALID = Regex("^[a-z]+([-' ][a-z]+)*$")
    }
}
