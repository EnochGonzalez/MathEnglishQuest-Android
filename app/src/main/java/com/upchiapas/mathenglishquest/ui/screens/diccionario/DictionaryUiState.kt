package com.upchiapas.mathenglishquest.ui.screens.diccionario

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

data class DictionaryUiState(
    val query: String = "",
    val status: DictionaryStatus = DictionaryStatus.IDLE,
    val entry: WordEntry? = null,
    /** true si no hubo internet y se muestra lo guardado de una búsqueda anterior. */
    val fromCache: Boolean = false,
    /** La palabra que se buscó, para mensajes como «No encontramos "x"». */
    val searchedWord: String = "",
    val serverErrorCode: Int? = null,
    val history: List<String> = emptyList()
)
