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
        val numbers = when (category) {
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

        _uiState.value = NumerosUiState(
            selectedCategory = category,
            numbersList = numbers
        )
    }
}
