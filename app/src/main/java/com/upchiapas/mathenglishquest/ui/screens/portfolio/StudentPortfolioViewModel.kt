package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.domain.clase.ClassDataSource
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.io.File

data class StudentPortfolioUiState(
    val studentName: String? = null,
    /** true si ese estudiante no es de la clase del maestro con la sesión abierta. */
    val accessDenied: Boolean = false,
    val portfolio: PortfolioUiState = PortfolioUiState()
)

/** El maestro mira (sin poder cambiar nada) el portafolio de uno de sus estudiantes. */
class StudentPortfolioViewModel(
    private val studentId: Int,
    private val classData: ClassDataSource
) : ViewModel() {

    private val difficultyFilter = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow(StudentPortfolioUiState())
    val uiState: StateFlow<StudentPortfolioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // Solo se puede ver a estudiantes de la propia clase
            val name = classData.studentName(studentId)
            if (name == null) {
                _uiState.value = StudentPortfolioUiState(
                    accessDenied = true,
                    portfolio = PortfolioUiState(isLoading = false)
                )
                return@launch
            }
            combine(classData.observeStudentEvidence(studentId), difficultyFilter) { list, filter ->
                buildPortfolioState(list, filter)
            }.collect { portfolio ->
                _uiState.value = StudentPortfolioUiState(studentName = name, portfolio = portfolio)
            }
        }
    }

    fun onDifficultyFilterSelected(difficultyName: String?) {
        difficultyFilter.value = difficultyName
    }

    fun fileFor(entry: EvidenceEntry): File = classData.fileFor(entry)
}
