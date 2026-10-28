package com.upchiapas.mathenglishquest.ui.screens.numeros

data class NumberItem(
    val number: Int,
    val englishText: String
)

enum class NumberCategory(val title: String) {
    BASICOS("Básicos"),
    DECENAS("Decenas"),
    CENTENAS("Centenas"),
    MILES("Miles")
}

data class NumerosUiState(
    val selectedCategory: NumberCategory = NumberCategory.BASICOS,
    val numbersList: List<NumberItem> = emptyList()
)
