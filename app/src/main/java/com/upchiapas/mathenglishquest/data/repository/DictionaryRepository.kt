package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryLocalStore
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryRemote
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
import com.upchiapas.mathenglishquest.domain.dictionary.PronunciationCache
import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import com.upchiapas.mathenglishquest.domain.dictionary.TextTranslator
import com.upchiapas.mathenglishquest.domain.dictionary.TranslatedEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordDefinition
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordMeaning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Único punto por el que la app consulta el diccionario: valida la palabra, la pide a la API externa
 * y guarda en el celular lo que sirvió, para poder verlo después sin internet.
 * También prepara el audio de pronunciación y la traducción al español.
 */
class DictionaryRepository(
    private val remote: DictionaryRemote,
    private val store: DictionaryLocalStore,
    private val pronunciation: PronunciationCache? = null,
    private val translator: TextTranslator? = null,
    /** Cuánto se espera a que el traductor esté listo (la primera vez baja el idioma). */
    private val prepareTimeoutMs: Long = 30_000,
    /** Cuánto se espera a que traduzca una palabra o una frase. */
    private val translateTimeoutMs: Long = 10_000
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

    /**
     * Deja el audio de la palabra guardado en el celular y devuelve su ruta (null si la palabra no trae audio
     * o no se pudo descargar). Si ya estaba guardado, no vuelve a descargarlo.
     */
    suspend fun preparePronunciation(entry: WordEntry): String? {
        val url = entry.audioUrl ?: return null
        val cache = pronunciation ?: return null
        return cache.cachedPath(url) ?: cache.download(url)
    }

    /** Pide al traductor que se prepare por adelantado (la primera vez baja el idioma, unos 30 MB). */
    suspend fun warmUpTranslator() {
        val translator = translator ?: return
        // Con tiempo límite: si la descarga se atora, no deja el traductor bloqueado para todos
        withTimeoutOrNull(prepareTimeoutMs) { translator.prepare() }
    }

    /**
     * La palabra y sus significados en español. Primero busca lo ya guardado (así funciona sin internet);
     * si no, traduce y lo guarda. Si una frase no se puede traducir se deja en inglés, y en ese caso no se guarda
     * para que se intente completar la próxima vez. Devuelve null si el traductor no está disponible.
     */
    suspend fun translateToSpanish(query: String, entry: WordEntry): TranslatedEntry? {
        val key = normalize(query) ?: entry.word.lowercase()
        store.loadTranslation(key)?.let { return it }

        val translator = translator ?: return null
        // Si no está listo a tiempo (o falla), se devuelve null: la pantalla muestra el aviso con "Reintentar"
        if (withTimeoutOrNull(prepareTimeoutMs) { translator.prepare() } != true) return null
        val translatedWord = withTimeoutOrNull(translateTimeoutMs) { translator.translate(entry.word) } ?: return null

        var complete = true
        suspend fun piece(text: String): String =
            withTimeoutOrNull(translateTimeoutMs) { translator.translate(text) } ?: text.also { complete = false }

        val meanings = entry.meanings.map { meaning ->
            WordMeaning(
                partOfSpeech = meaning.partOfSpeech,
                definitions = meaning.definitions.map { definition ->
                    WordDefinition(
                        text = piece(definition.text),
                        example = definition.example?.let { piece(it) }
                    )
                }
            )
        }
        val result = TranslatedEntry(word = translatedWord, meanings = meanings)
        if (complete) store.saveTranslation(key, result)
        return result
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
