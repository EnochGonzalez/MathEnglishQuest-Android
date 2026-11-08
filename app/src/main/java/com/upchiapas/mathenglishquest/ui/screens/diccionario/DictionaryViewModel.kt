package com.upchiapas.mathenglishquest.ui.screens.diccionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.DictionaryRepository
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Une la pantalla del diccionario con el repositorio: recibe lo que escribe el usuario y publica el estado. */
class DictionaryViewModel(
    private val repository: DictionaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DictionaryUiState(history = repository.history.value))
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var extrasJob: Job? = null     // audio y traducción de la palabra que se está mostrando

    init {
        // El historial vive en el repositorio (y en el celular): aquí solo se refleja en la pantalla
        viewModelScope.launch {
            repository.history.collect { words -> _uiState.update { it.copy(history = words) } }
        }
        // Se avisa al traductor desde que se abre la pantalla: la primera vez baja el idioma mientras el usuario escribe
        viewModelScope.launch { repository.warmUpTranslator() }
    }

    fun onQueryChange(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    /** Buscar lo que está escrito. También sirve para "Reintentar" después de un error. */
    fun onSearch() = search(_uiState.value.query)

    fun onHistoryWordSelected(word: String) {
        _uiState.update { it.copy(query = word) }
        search(word)
    }

    fun onHistoryWordRemoved(word: String) = repository.removeFromHistory(word)

    fun onClearHistory() = repository.clearHistory()

    fun onLanguageSelected(spanish: Boolean) {
        _uiState.update { it.copy(showSpanish = spanish) }
    }

    /** "Reintentar traducción": vuelve a pedir el audio y la traducción de la palabra que se está mostrando. */
    fun onRetryTranslation() {
        val state = _uiState.value
        val entry = state.entry ?: return
        loadExtras(entry, state.searchedWord)
    }

    private fun search(raw: String) {
        searchJob?.cancel()                         // si se busca otra palabra, la anterior ya no importa
        extrasJob?.cancel()
        val word = raw.trim()
        if (word.isEmpty()) {
            _uiState.update { it.copy(status = DictionaryStatus.INVALID, entry = null, searchedWord = "") }
            return
        }
        _uiState.update {
            it.copy(
                status = DictionaryStatus.LOADING,
                searchedWord = word,
                entry = null,
                fromCache = false,
                audioFilePath = null,
                translation = null,
                translationStatus = TranslationStatus.IDLE
            )
        }
        searchJob = viewModelScope.launch {
            val result = repository.lookup(word)
            _uiState.update { state ->
                when (result) {
                    is LookupResult.Success -> state.copy(
                        status = DictionaryStatus.SUCCESS,
                        entry = result.entry,
                        fromCache = result.fromCache,
                        translationStatus = TranslationStatus.LOADING
                    )
                    LookupResult.InvalidInput -> state.copy(status = DictionaryStatus.INVALID)
                    LookupResult.NotFound -> state.copy(status = DictionaryStatus.NOT_FOUND)
                    LookupResult.NoConnection -> state.copy(status = DictionaryStatus.NO_CONNECTION)
                    is LookupResult.ServerError ->
                        state.copy(status = DictionaryStatus.SERVER_ERROR, serverErrorCode = result.code)
                }
            }
            if (result is LookupResult.Success) loadExtras(result.entry, word)
        }
    }

    /**
     * Con la palabra ya en pantalla se preparan, sin hacer esperar al usuario, su audio (se descarga y se guarda para
     * que el 🔊 suene al instante) y su traducción al español. Si llegan tarde y ya se muestra otra palabra, se ignoran.
     */
    private fun loadExtras(entry: WordEntry, query: String) {
        extrasJob?.cancel()
        _uiState.update { it.copy(translationStatus = TranslationStatus.LOADING) }
        extrasJob = viewModelScope.launch {
            launch {
                val path = repository.preparePronunciation(entry)
                _uiState.update { if (it.entry == entry) it.copy(audioFilePath = path) else it }
            }
            launch {
                val translation = repository.translateToSpanish(query, entry)
                _uiState.update {
                    if (it.entry != entry) it
                    else it.copy(
                        translation = translation,
                        translationStatus = if (translation != null) TranslationStatus.DONE else TranslationStatus.UNAVAILABLE
                    )
                }
            }
        }
    }
}
