package com.upchiapas.mathenglishquest.ui.screens.diccionario

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.DictionaryRepository
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
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

    init {
        // El historial vive en el repositorio (y en el celular): aquí solo se refleja en la pantalla
        viewModelScope.launch {
            repository.history.collect { words -> _uiState.update { it.copy(history = words) } }
        }
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

    private fun search(raw: String) {
        searchJob?.cancel()                         // si se busca otra palabra, la anterior ya no importa
        val word = raw.trim()
        if (word.isEmpty()) {
            _uiState.update { it.copy(status = DictionaryStatus.INVALID, entry = null, searchedWord = "") }
            return
        }
        _uiState.update {
            it.copy(status = DictionaryStatus.LOADING, searchedWord = word, entry = null, fromCache = false)
        }
        searchJob = viewModelScope.launch {
            val result = repository.lookup(word)
            _uiState.update { state ->
                when (result) {
                    is LookupResult.Success ->
                        state.copy(status = DictionaryStatus.SUCCESS, entry = result.entry, fromCache = result.fromCache)
                    LookupResult.InvalidInput -> state.copy(status = DictionaryStatus.INVALID)
                    LookupResult.NotFound -> state.copy(status = DictionaryStatus.NOT_FOUND)
                    LookupResult.NoConnection -> state.copy(status = DictionaryStatus.NO_CONNECTION)
                    is LookupResult.ServerError ->
                        state.copy(status = DictionaryStatus.SERVER_ERROR, serverErrorCode = result.code)
                }
            }
        }
    }
}
