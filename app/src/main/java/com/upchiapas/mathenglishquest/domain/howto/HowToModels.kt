package com.upchiapas.mathenglishquest.domain.howto

/** Una fila de una tabla de operaciones en columna. [cells] tiene un texto por columna ("" = vacío). */
data class TableRow(
    /** Símbolo de la fila: "", "+", "-" o "×". */
    val op: String,
    val cells: List<String>,
    /** Raya arriba de la fila (la raya de la suma). */
    val hasLine: Boolean = false,
    /** Fila del resultado (se pinta en azul). */
    val isResult: Boolean = false
)

/** Una fila de la casita (un producto que se resta, o el número que queda al bajar una cifra). */
data class DivisionRow(
    val cells: List<String>,
    /** Raya debajo de la fila, de la columna [underlineFromCol] a la [underlineToCol]; -1 = sin raya. */
    val underlineFromCol: Int = -1,
    val underlineToCol: Int = -1
)

enum class HowToTextStyle {
    /** Una frase del paso a paso, alineada a la izquierda ("En 22, el 5 cabe 4 veces..."). */
    STEP,
    /** Texto pequeño que explica un paso. */
    LABEL,
    /** Aclaración centrada (signos, decimales, redondeo). */
    NOTE,
    /** "Resultado: ..." */
    RESULT
}

/** Pieza de un procedimiento. La pantalla solo sabe dibujar estas piezas. */
sealed interface HowToBlock {
    /** Tabla en columna. [carryRow] = acarreos (arriba, en rojo). [borrowRow] = lo que se "pide prestado" en una resta. */
    data class Table(
        val rows: List<TableRow>,
        val carryRow: List<String>? = null,
        val borrowRow: List<String>? = null
    ) : HowToBlock

    data class Text(val text: String, val style: HowToTextStyle) : HowToBlock

    /**
     * La división larga como en el cuaderno: el cociente arriba, el divisor a la izquierda, el dividendo adentro
     * y debajo las restas, bajando cifras. Todo vive en una cuadrícula de columnas ([columnIsDot] dice cuáles
     * son de punto decimal, que son más angostas); la columna 0 es solo para el signo "−".
     */
    data class DivisionCasita(
        val divisor: String,
        val columnIsDot: List<Boolean>,
        val quotient: List<String>,
        val dividend: List<String>,
        /** Primera columna con ceros que se agregaron al dividendo (se dibujan atenuados); -1 si no hay. */
        val appendedFromCol: Int,
        val rows: List<DivisionRow>
    ) : HowToBlock
}

/** El procedimiento de un paso, con el título que se muestra encima ("Paso 1: Suma — 3 + 4"). */
data class HowToSolution(
    val title: String,
    val blocks: List<HowToBlock>
)
