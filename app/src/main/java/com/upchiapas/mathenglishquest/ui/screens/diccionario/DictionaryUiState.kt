package com.upchiapas.mathenglishquest.ui.screens.diccionario

import com.upchiapas.mathenglishquest.domain.dictionary.TranslatedEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry

enum class DictionaryStatus {
    /** Todavía no se ha buscado nada. */
    IDLE,
    LOADING,
    SUCCESS,
    NOT_FOUND,
    /** Lo escrito no parece una palabra en inglés. */
    INVALID,
    NO_CONNECTION,
    SERVER_ERROR
}

/** Cómo va la traducción al español de la palabra que se muestra. */
enum class TranslationStatus {
    IDLE,
    LOADING,
    DONE,
    /** No se pudo traducir (por ejemplo, sin internet la primera vez, cuando todavía falta bajar el idioma). */
    UNAVAILABLE
}

data class DictionaryUiState(
    val query: String = "",
    val status: DictionaryStatus = DictionaryStatus.IDLE,
    val entry: WordEntry? = null,
    /** true si no hubo internet y se muestra lo guardado de una búsqueda anterior. */
    val fromCache: Boolean = false,
    /** La palabra que se buscó, para mensajes como «No encontramos "x"». */
    val searchedWord: String = "",
    val serverErrorCode: Int? = null,
    val history: List<String> = emptyList(),
    /** Ruta del audio ya guardado en el celular (suena al instante); null mientras se descarga o si no hay. */
    val audioFilePath: String? = null,
    val translation: TranslatedEntry? = null,
    val translationStatus: TranslationStatus = TranslationStatus.IDLE,
    /** true = se muestra el significado en español (cuando ya está traducido). */
    val showSpanish: Boolean = true
)
