package com.upchiapas.mathenglishquest.data.model

/** Operaciones del módulo del Dado. [symbol] es lo que se muestra en pantalla (OP_SYMBOL del prototipo). */
enum class Operation(val symbol: String, val category: String) {
    SUMA("+", "Suma"),
    RESTA("-", "Resta"),
    MULTIPLICACION("×", "Multiplicación"),
    DIVISION("÷", "División");

    val isMultiplicative: Boolean get() = this == MULTIPLICACION || this == DIVISION
}
