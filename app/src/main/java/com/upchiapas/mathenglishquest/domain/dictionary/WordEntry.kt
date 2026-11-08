package com.upchiapas.mathenglishquest.domain.dictionary

/** Una definición de una palabra, con un ejemplo si la API lo trae. */
data class WordDefinition(
    val text: String,
    val example: String? = null
)

/** Un significado de la palabra según su categoría gramatical (sustantivo, verbo...). */
data class WordMeaning(
    val partOfSpeech: String,
    val definitions: List<WordDefinition>
)

/** Lo que la app muestra de una palabra del diccionario. */
data class WordEntry(
    val word: String,
    /** Cómo se pronuncia, escrito (por ejemplo "/fɔːˈtiːn/"). */
    val phonetic: String? = null,
    /** Dirección de un audio con la pronunciación; null si la API no trae ninguno. */
    val audioUrl: String? = null,
    val meanings: List<WordMeaning> = emptyList()
)
