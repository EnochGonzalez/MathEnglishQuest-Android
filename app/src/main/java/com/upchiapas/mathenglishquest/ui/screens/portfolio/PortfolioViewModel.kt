package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.domain.portfolio.DayGroup
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import com.upchiapas.mathenglishquest.domain.portfolio.PortfolioFilter
import com.upchiapas.mathenglishquest.domain.portfolio.PortfolioGrouping
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.io.File

data class PortfolioUiState(
    /** Evidencias ya filtradas y agrupadas por día (la más reciente primero). */
    val groups: List<DayGroup> = emptyList(),
    /** Dificultad elegida en los filtros; null = todas. */
    val difficultyFilter: String? = null,
    /** Total de evidencias sin filtrar (para distinguir "no hay nada" de "no hay nada con este filtro"). */
    val totalCount: Int = 0,
    val isLoading: Boolean = true
)

fun buildPortfolioState(entries: List<EvidenceEntry>, difficultyFilter: String?) = PortfolioUiState(
    groups = PortfolioGrouping.groupByDay(PortfolioFilter.byDifficulty(entries, difficultyFilter)),
    difficultyFilter = difficultyFilter,
    totalCount = entries.size,
    isLoading = false
)

/** Portafolio del estudiante con la sesión abierta. */
class PortfolioViewModel(
    entries: Flow<List<EvidenceEntry>>,
    private val fileResolver: (EvidenceEntry) -> File
) : ViewModel() {

    private val difficultyFilter = MutableStateFlow<String?>(null)

    /** Se actualiza sola cada vez que se guarda una foto nueva o cambia el filtro. */
    val uiState: StateFlow<PortfolioUiState> = combine(entries, difficultyFilter) { list, filter ->
        buildPortfolioState(list, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PortfolioUiState()
    )

    fun onDifficultyFilterSelected(difficultyName: String?) {
        difficultyFilter.value = difficultyName
    }

    fun fileFor(entry: EvidenceEntry): File = fileResolver(entry)
}
