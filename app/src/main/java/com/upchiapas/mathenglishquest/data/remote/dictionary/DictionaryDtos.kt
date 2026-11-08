package com.upchiapas.mathenglishquest.data.remote.dictionary

/**
 * Forma de la respuesta de https://api.dictionaryapi.dev (Free Dictionary API).
 * Todo es opcional porque la API no siempre manda todos los campos. Los valores por defecto permiten que Gson
 * deje en null (y no se caiga) lo que falte.
 */
data class EntryDto(
    val word: String? = null,
    val phonetic: String? = null,
    val phonetics: List<PhoneticDto>? = null,
    val meanings: List<MeaningDto>? = null
)

data class PhoneticDto(
    val text: String? = null,
    val audio: String? = null
)

data class MeaningDto(
    val partOfSpeech: String? = null,
    val definitions: List<DefinitionDto>? = null
)

data class DefinitionDto(
    val definition: String? = null,
    val example: String? = null
)
