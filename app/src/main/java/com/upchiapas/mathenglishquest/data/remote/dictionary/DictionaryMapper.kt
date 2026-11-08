package com.upchiapas.mathenglishquest.data.remote.dictionary

import com.upchiapas.mathenglishquest.domain.dictionary.WordDefinition
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordMeaning

/** Convierte lo que manda la API en lo que muestra la app: se queda con lo útil y lo ordena. */
object DictionaryMapper {

    private const val MAX_MEANINGS = 4
    private const val MAX_DEFINITIONS_PER_MEANING = 3

    private val PARTS_OF_SPEECH = mapOf(
        "noun" to "Sustantivo",
        "verb" to "Verbo",
        "adjective" to "Adjetivo",
        "adverb" to "Adverbio",
        "pronoun" to "Pronombre",
        "preposition" to "Preposición",
        "conjunction" to "Conjunción",
        "interjection" to "Interjección",
        "numeral" to "Numeral",
        "article" to "Artículo",
        "determiner" to "Determinante",
        "exclamation" to "Exclamación"
    )

    /** La API puede devolver varias entradas de la misma palabra: se juntan en una sola. Null si no hay nada útil. */
    fun toWordEntry(entries: List<EntryDto>?): WordEntry? {
        if (entries.isNullOrEmpty()) return null

        val word = entries.firstNotNullOfOrNull { it.word?.trim()?.takeIf(String::isNotEmpty) } ?: return null

        val phonetic = entries.firstNotNullOfOrNull { entry ->
            entry.phonetic?.takeIf(String::isNotBlank)
                ?: entry.phonetics.orEmpty().firstNotNullOfOrNull { it.text?.takeIf(String::isNotBlank) }
        }

        val audio = entries
            .flatMap { it.phonetics.orEmpty() }
            .firstNotNullOfOrNull { it.audio?.takeIf(String::isNotBlank) }
            ?.let(::normalizeUrl)

        val meanings = entries
            .flatMap { it.meanings.orEmpty() }
            .mapNotNull(::toMeaning)
            .distinctBy { it.partOfSpeech }
            .take(MAX_MEANINGS)

        if (meanings.isEmpty() && phonetic == null && audio == null) return null
        return WordEntry(word = word, phonetic = phonetic, audioUrl = audio, meanings = meanings)
    }

    private fun toMeaning(dto: MeaningDto): WordMeaning? {
        val definitions = dto.definitions.orEmpty()
            .mapNotNull { d ->
                val text = d.definition?.trim()?.takeIf(String::isNotEmpty) ?: return@mapNotNull null
                WordDefinition(text = text, example = d.example?.trim()?.takeIf(String::isNotEmpty))
            }
            .take(MAX_DEFINITIONS_PER_MEANING)
        if (definitions.isEmpty()) return null
        return WordMeaning(partOfSpeech = translatePartOfSpeech(dto.partOfSpeech), definitions = definitions)
    }

    private fun translatePartOfSpeech(value: String?): String {
        val key = value?.trim()?.lowercase().orEmpty()
        if (key.isEmpty()) return "Palabra"
        return PARTS_OF_SPEECH[key] ?: key.replaceFirstChar { it.uppercase() }
    }

    /** Algunos audios vienen como "//dominio/archivo.mp3" (sin https). */
    private fun normalizeUrl(url: String): String = if (url.startsWith("//")) "https:$url" else url
}
