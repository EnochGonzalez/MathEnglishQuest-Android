package com.upchiapas.mathenglishquest.ui.screens.clase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.upchiapas.mathenglishquest.domain.clase.ClassDataSource
import com.upchiapas.mathenglishquest.domain.clase.ClassRoster
import com.upchiapas.mathenglishquest.domain.clase.RosterOrder
import com.upchiapas.mathenglishquest.domain.clase.StudentSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClassUiState(
    /** null = quien entró no es maestro, o todavía no se sabe (mira [isLoading]). */
    val classCode: String? = null,
    /** Estudiantes ya buscados y ordenados. */
    val students: List<StudentSummary> = emptyList(),
    /** Total de estudiantes de la clase, sin filtrar. */
    val totalStudents: Int = 0,
    val query: String = "",
    val order: RosterOrder = RosterOrder.NOMBRE,
    val isLoading: Boolean = true
)

class ClassViewModel(
    private val classData: ClassDataSource
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val order = MutableStateFlow(RosterOrder.NOMBRE)
    private val _uiState = MutableStateFlow(ClassUiState())
    val uiState: StateFlow<ClassUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val code = classData.myClassCode()
            if (code == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            // La lista se actualiza sola cuando un estudiante se registra o sube una foto
            combine(classData.observeRoster(code), query, order) { roster, q, o ->
                ClassUiState(
                    classCode = code,
                    students = ClassRoster.sort(ClassRoster.filter(roster, q), o),
                    totalStudents = roster.size,
                    query = q,
                    order = o,
                    isLoading = false
                )
            }.collect { _uiState.value = it }
        }
    }

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun onOrderSelected(value: RosterOrder) {
        order.value = value
    }
}
