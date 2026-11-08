package com.upchiapas.mathenglishquest.data.remote.dictionary

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryMapperTest {

    private fun definition(text: String?, example: String? = null) = DefinitionDto(text, example)

    private val hello = EntryDto(
        word = "hello",
        phonetic = "/həˈləʊ/",
        phonetics = listOf(
            PhoneticDto(text = "/həˈləʊ/", audio = ""),
            PhoneticDto(text = "/həˈloʊ/", audio = "https://api.dictionaryapi.dev/media/pronunciations/en/hello-us.mp3")
        ),
        meanings = listOf(
            MeaningDto("noun", listOf(definition("\"Hello!\" or an equivalent greeting.", "He said hello."))),
            MeaningDto("verb", listOf(definition("To greet with \"hello\".")))
        )
    )

    @Test
    fun mapea_lasPartesUtilesDeUnaEntrada() {
        val entry = DictionaryMapper.toWordEntry(listOf(hello))!!
        assertEquals("hello", entry.word)
        assertEquals("/həˈləʊ/", entry.phonetic)
        assertEquals(2, entry.meanings.size)
        assertEquals("Sustantivo", entry.meanings[0].partOfSpeech)
        assertEquals("Verbo", entry.meanings[1].partOfSpeech)
        assertEquals("He said hello.", entry.meanings[0].definitions[0].example)
    }

    @Test
    fun audio_tomaElPrimeroQueNoEsteVacio() {
        val entry = DictionaryMapper.toWordEntry(listOf(hello))!!
        assertEquals("https://api.dictionaryapi.dev/media/pronunciations/en/hello-us.mp3", entry.audioUrl)
    }

    @Test
    fun audio_completaElHttpsCuandoVieneSinEl() {
        val dto = EntryDto("a", phonetics = listOf(PhoneticDto(audio = "//cdn.example.com/a.mp3")),
            meanings = listOf(MeaningDto("noun", listOf(definition("x")))))
        assertEquals("https://cdn.example.com/a.mp3", DictionaryMapper.toWordEntry(listOf(dto))!!.audioUrl)
    }

    @Test
    fun sinAudio_quedaNull() {
        val dto = EntryDto("a", meanings = listOf(MeaningDto("noun", listOf(definition("x")))))
        assertNull(DictionaryMapper.toWordEntry(listOf(dto))!!.audioUrl)
    }

    @Test
    fun fonetica_siNoHayEnLaEntradaSeBuscaEnLasFoneticas() {
        val dto = EntryDto("a", phonetics = listOf(PhoneticDto(text = "/eɪ/")),
            meanings = listOf(MeaningDto("noun", listOf(definition("x")))))
        assertEquals("/eɪ/", DictionaryMapper.toWordEntry(listOf(dto))!!.phonetic)
    }

    @Test
    fun varias_entradasSeJuntanEnUna() {
        val a = EntryDto("run", meanings = listOf(MeaningDto("verb", listOf(definition("To move fast")))))
        val b = EntryDto("run", meanings = listOf(MeaningDto("noun", listOf(definition("An act of running")))))
        val entry = DictionaryMapper.toWordEntry(listOf(a, b))!!
        assertEquals(listOf("Verbo", "Sustantivo"), entry.meanings.map { it.partOfSpeech })
    }

    @Test
    fun categoriasRepetidasSeQuedanConLaPrimera() {
        val a = EntryDto("x", meanings = listOf(MeaningDto("noun", listOf(definition("uno")))))
        val b = EntryDto("x", meanings = listOf(MeaningDto("noun", listOf(definition("dos")))))
        val entry = DictionaryMapper.toWordEntry(listOf(a, b))!!
        assertEquals(1, entry.meanings.size)
        assertEquals("uno", entry.meanings[0].definitions[0].text)
    }

    @Test
    fun limita_significadosYDefiniciones() {
        val defs = (1..8).map { definition("def $it") }
        val meanings = listOf("noun", "verb", "adjective", "adverb", "pronoun", "numeral").map { MeaningDto(it, defs) }
        val entry = DictionaryMapper.toWordEntry(listOf(EntryDto("x", meanings = meanings)))!!
        assertEquals(4, entry.meanings.size)
        assertTrue(entry.meanings.all { it.definitions.size == 3 })
    }

    @Test
    fun ignora_definicionesVaciasYSignificadosSinDefiniciones() {
        val dto = EntryDto("x", meanings = listOf(
            MeaningDto("noun", listOf(definition(null), definition("   "))),
            MeaningDto("verb", listOf(definition("Sí hay")))
        ))
        val entry = DictionaryMapper.toWordEntry(listOf(dto))!!
        assertEquals(listOf("Verbo"), entry.meanings.map { it.partOfSpeech })
    }

    @Test
    fun categoriaDesconocidaSeMuestraConMayuscula() {
        val dto = EntryDto("x", meanings = listOf(MeaningDto("phrase", listOf(definition("algo")))))
        assertEquals("Phrase", DictionaryMapper.toWordEntry(listOf(dto))!!.meanings[0].partOfSpeech)
        val sin = EntryDto("x", meanings = listOf(MeaningDto(null, listOf(definition("algo")))))
        assertEquals("Palabra", DictionaryMapper.toWordEntry(listOf(sin))!!.meanings[0].partOfSpeech)
    }

    @Test
    fun respuestasSinNadaUtilDanNull() {
        assertNull(DictionaryMapper.toWordEntry(null))
        assertNull(DictionaryMapper.toWordEntry(emptyList()))
        assertNull(DictionaryMapper.toWordEntry(listOf(EntryDto())))                    // sin palabra
        assertNull(DictionaryMapper.toWordEntry(listOf(EntryDto(word = "x"))))          // sin significados ni fonética
        assertNotNull(DictionaryMapper.toWordEntry(listOf(EntryDto(word = "x", phonetic = "/x/"))))
    }
}
