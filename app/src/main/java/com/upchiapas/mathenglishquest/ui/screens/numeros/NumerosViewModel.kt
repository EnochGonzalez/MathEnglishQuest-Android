package com.upchiapas.mathenglishquest.ui.screens.numeros

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NumerosViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NumerosUiState())
    val uiState: StateFlow<NumerosUiState> = _uiState.asStateFlow()

    init {
        selectCategory(NumberCategory.BASICOS)
    }

    fun selectCategory(category: NumberCategory) {
        val numbers = getCategoryNumbers(category)
        _uiState.value = NumerosUiState(
            selectedCategory = category,
            selectedDecade = null,
            numbersList = numbers
        )
    }

    fun onNumberCardClick(item: NumberItem): Boolean {
        val state = _uiState.value
        // Si estamos en la categoría Decenas y aún no se ha desplegado una decena
        if (state.selectedCategory == NumberCategory.DECENAS && state.selectedDecade == null) {
            if (item.number in 20..90 && item.number % 10 == 0) {
                selectDecade(item.number)
                return false // Indica que fue una acción de navegación, no solo audio
            }
        }
        return true // Reproduce el audio normalmente
    }

    fun selectDecade(decade: Int) {
        val breakdown = getDecadeBreakdown(decade)
        _uiState.value = _uiState.value.copy(
            selectedDecade = decade,
            numbersList = breakdown
        )
    }

    fun clearSelectedDecade() {
        val category = _uiState.value.selectedCategory
        _uiState.value = _uiState.value.copy(
            selectedDecade = null,
            numbersList = getCategoryNumbers(category)
        )
    }

    private fun getCategoryNumbers(category: NumberCategory): List<NumberItem> {
        return when (category) {
            NumberCategory.BASICOS -> listOf(
                NumberItem(1, "One"),
                NumberItem(2, "Two"),
                NumberItem(3, "Three"),
                NumberItem(4, "Four"),
                NumberItem(5, "Five"),
                NumberItem(6, "Six"),
                NumberItem(7, "Seven"),
                NumberItem(8, "Eight"),
                NumberItem(9, "Nine"),
                NumberItem(10, "Ten"),
                NumberItem(11, "Eleven"),
                NumberItem(12, "Twelve"),
                NumberItem(13, "Thirteen"),
                NumberItem(14, "Fourteen"),
                NumberItem(15, "Fifteen"),
                NumberItem(16, "Sixteen"),
                NumberItem(17, "Seventeen"),
                NumberItem(18, "Eighteen"),
                NumberItem(19, "Nineteen")
            )

            NumberCategory.DECENAS -> listOf(
                NumberItem(20, "Twenty"),
                NumberItem(30, "Thirty"),
                NumberItem(40, "Forty"),
                NumberItem(50, "Fifty"),
                NumberItem(60, "Sixty"),
                NumberItem(70, "Seventy"),
                NumberItem(80, "Eighty"),
                NumberItem(90, "Ninety"),
                NumberItem(100, "One Hundred")
            )

            NumberCategory.CENTENAS -> listOf(
                NumberItem(100, "One Hundred"),
                NumberItem(200, "Two Hundred"),
                NumberItem(300, "Three Hundred"),
                NumberItem(400, "Four Hundred"),
                NumberItem(500, "Five Hundred"),
                NumberItem(600, "Six Hundred"),
                NumberItem(700, "Seven Hundred"),
                NumberItem(800, "Eight Hundred"),
                NumberItem(900, "Nine Hundred")
            )

            NumberCategory.MILES -> listOf(
                NumberItem(1000, "One Thousand"),
                NumberItem(2000, "Two Thousand"),
                NumberItem(3000, "Three Thousand"),
                NumberItem(4000, "Four Thousand"),
                NumberItem(5000, "Five Thousand"),
                NumberItem(6000, "Six Thousand"),
                NumberItem(7000, "Seven Thousand"),
                NumberItem(8000, "Eight Thousand"),
                NumberItem(9000, "Nine Thousand")
            )
        }
    }

    private fun getDecadeBreakdown(decade: Int): List<NumberItem> {
        val prefix = when (decade) {
            20 -> "Twenty"
            30 -> "Thirty"
            40 -> "Forty"
            50 -> "Fifty"
            60 -> "Sixty"
            70 -> "Seventy"
            80 -> "Eighty"
            90 -> "Ninety"
            else -> return emptyList()
        }
        val units = listOf("Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine")
        return (0..9).map { i ->
            val num = decade + i
            val text = if (i == 0) prefix else "$prefix-${units[i]}"
            NumberItem(num, text)
        }
    }
}
