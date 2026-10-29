package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.data.repository.EvidenceRepository
import com.upchiapas.mathenglishquest.domain.portfolio.DayGroup
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import com.upchiapas.mathenglishquest.domain.portfolio.PortfolioGrouping
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File

data class PortfolioUiState(
    val groups: List<DayGroup> = emptyList(),
    val isLoading: Boolean = true
)

class PortfolioViewModel(
    private val evidenceRepository: EvidenceRepository
) : ViewModel() {

    /** Se actualiza sola cada vez que se guarda una foto nueva. */
    val uiState: StateFlow<PortfolioUiState> = evidenceRepository.observeMine()
        .map { entries -> PortfolioUiState(groups = PortfolioGrouping.groupByDay(entries), isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PortfolioUiState()
        )

    fun fileFor(entry: EvidenceEntry): File = evidenceRepository.fileFor(entry)
}
