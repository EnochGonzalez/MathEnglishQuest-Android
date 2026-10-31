package com.upchiapas.mathenglishquest.ui.screens.numeros

import androidx.annotation.StringRes
import com.upchiapas.mathenglishquest.R

data class NumberItem(
    val number: Int,
    val englishText: String
)

enum class NumberCategory(@StringRes val titleRes: Int) {
    BASICOS(R.string.category_basicos),
    DECENAS(R.string.category_decenas),
    CENTENAS(R.string.category_centenas),
    MILES(R.string.category_miles)
}

data class NumerosUiState(
    val selectedCategory: NumberCategory = NumberCategory.BASICOS,
    val selectedDecade: Int? = null,
    val numbersList: List<NumberItem> = emptyList()
)
