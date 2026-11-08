package com.upchiapas.mathenglishquest.domain.dictionary

/** Respuesta de la API ya traducida a algo que la app entiende (sin saber de Retrofit ni de HTTP). */
sealed interface RemoteLookup {
    data class Found(val entry: WordEntry) : RemoteLookup
    /** La API respondió que esa palabra no existe. */
    data object NotFound : RemoteLookup
    /** No se pudo llegar al servidor (sin internet, tiempo agotado...). */
    data object NoConnection : RemoteLookup
    /** El servidor respondió con un error ([code] es el código HTTP, si lo hay). */
    data class ServerError(val code: Int?) : RemoteLookup
}

/** Quien sabe preguntarle una palabra a la API externa. */
interface DictionaryRemote {
    suspend fun lookup(word: String): RemoteLookup
}

/** Dónde se guardan en el celular las palabras buscadas, para verlas sin internet. */
interface DictionaryLocalStore {
    fun loadHistory(): List<String>
    fun saveHistory(words: List<String>)
    fun loadEntry(word: String): WordEntry?
    fun saveEntry(word: String, entry: WordEntry)
    fun removeEntry(word: String)
}

/** Resultado de una búsqueda tal como lo necesita la pantalla. */
sealed interface LookupResult {
    /** [fromCache] = true si no hubo internet y se mostró lo guardado de una búsqueda anterior. */
    data class Success(val entry: WordEntry, val fromCache: Boolean) : LookupResult
    data object InvalidInput : LookupResult
    data object NotFound : LookupResult
    data object NoConnection : LookupResult
    data class ServerError(val code: Int?) : LookupResult
}
