package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryLocalStore
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryRemote
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
import com.upchiapas.mathenglishquest.domain.dictionary.PronunciationCache
import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import com.upchiapas.mathenglishquest.domain.dictionary.TextTranslator
import com.upchiapas.mathenglishquest.domain.dictionary.TranslatedEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordDefinition
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
import com.upchiapas.mathenglishquest.domain.dictionary.WordMeaning
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DictionaryRepositoryTest {

    private class FakeRemote(var next: RemoteLookup) : DictionaryRemote {
        val asked = mutableListOf<String>()
        override suspend fun lookup(word: String): RemoteLookup { asked += word; return next }
    }

    private class FakeStore(history: List<String> = emptyList()) : DictionaryLocalStore {
        var history = history
        val entries = mutableMapOf<String, WordEntry>()
        val translations = mutableMapOf<String, TranslatedEntry>()
        override fun loadHistory() = history
        override fun saveHistory(words: List<String>) { history = words }
        override fun loadEntry(word: String) = entries[word]
        override fun saveEntry(word: String, entry: WordEntry) { entries[word] = entry }
        override fun removeEntry(word: String) { entries.remove(word); translations.remove(word) }
        override fun loadTranslation(word: String) = translations[word]
        override fun saveTranslation(word: String, translation: TranslatedEntry) { translations[word] = translation }
    }

    private class FakeTranslator(var prepared: Boolean = true) : TextTranslator {
        val translated = mutableListOf<String>()
        var prepareCalls = 0
        val failOn = mutableSetOf<String>()
        override suspend fun prepare(): Boolean { prepareCalls++; return prepared }
        override suspend fun translate(text: String): String? {
            translated += text
            return if (text in failOn) null else "ES($text)"
        }
    }

    private class FakeAudio(var downloadResult: String? = "/audios/a.mp3") : PronunciationCache {
        val saved = mutableMapOf<String, String>()
        var downloads = 0
        override fun cachedPath(url: String) = saved[url]
        override suspend fun download(url: String): String? { downloads++; return downloadResult?.also { saved[url] = it } }
    }

    private fun full(word: String) = WordEntry(
        word = word, phonetic = "/x/", audioUrl = "https://audio/$word.mp3",
        meanings = listOf(WordMeaning("Numeral", listOf(WordDefinition("def one", "example one"), WordDefinition("def two"))))
    )

    private fun entry(word: String) = WordEntry(word = word, phonetic = "/x/")
    private fun repo(remote: FakeRemote, store: FakeStore = FakeStore()) = DictionaryRepository(remote, store)

    @Test
    fun busquedaExitosa_guardaEnHistorialYEnCache() = runBlocking {
        val store = FakeStore(); val r = repo(FakeRemote(RemoteLookup.Found(entry("seven"))), store)
        val result = r.lookup("seven")
        assertEquals(LookupResult.Success(entry("seven"), fromCache = false), result)
        assertEquals(listOf("seven"), r.history.value)
        assertEquals(listOf("seven"), store.history)             // quedó guardado en el celular
        assertEquals(entry("seven"), store.entries["seven"])
    }

    @Test
    fun normalizaLaPalabraAntesDePreguntar() = runBlocking {
        val remote = FakeRemote(RemoteLookup.Found(entry("ice cream")))
        repo(remote).lookup("  Ice   CREAM ")
        assertEquals(listOf("ice cream"), remote.asked)
    }

    @Test
    fun palabrasInvalidasNoLlegan_aLaApi() = runBlocking {
        val remote = FakeRemote(RemoteLookup.Found(entry("x")))
        val r = repo(remote)
        listOf("", "   ", "12", "hello1", "hola!", "niño", "a--b", "-a", "a b-", "x".repeat(41)).forEach {
            assertEquals("«$it»", LookupResult.InvalidInput, r.lookup(it))
        }
        assertTrue(remote.asked.isEmpty())
    }

    @Test
    fun palabrasValidasConGuionYApostrofo() = runBlocking {
        val remote = FakeRemote(RemoteLookup.Found(entry("x")))
        val r = repo(remote)
        listOf("twenty-one", "don't", "ice cream", "A").forEach { assertTrue("«$it»", r.lookup(it) is LookupResult.Success) }
    }

    @Test
    fun palabraInexistente_noSeGuarda() = runBlocking {
        val store = FakeStore(); val r = repo(FakeRemote(RemoteLookup.NotFound), store)
        assertEquals(LookupResult.NotFound, r.lookup("zzzz"))
        assertTrue(r.history.value.isEmpty()); assertTrue(store.entries.isEmpty())
    }

    @Test
    fun sinInternet_sinNadaGuardado() = runBlocking {
        assertEquals(LookupResult.NoConnection, repo(FakeRemote(RemoteLookup.NoConnection)).lookup("seven"))
    }

    @Test
    fun sinInternet_peroYaSeHabiaBuscado_muestraLoGuardado() = runBlocking {
        val store = FakeStore().apply { entries["seven"] = entry("seven") }
        val result = repo(FakeRemote(RemoteLookup.NoConnection), store).lookup("seven")
        assertEquals(LookupResult.Success(entry("seven"), fromCache = true), result)
    }

    @Test
    fun servidorCaido_conYSinCache() = runBlocking {
        assertEquals(LookupResult.ServerError(503), repo(FakeRemote(RemoteLookup.ServerError(503))).lookup("seven"))
        val store = FakeStore().apply { entries["seven"] = entry("seven") }
        val cached = repo(FakeRemote(RemoteLookup.ServerError(500)), store).lookup("seven")
        assertEquals(LookupResult.Success(entry("seven"), fromCache = true), cached)
    }

    @Test
    fun historial_laMasRecientePrimeroSinRepetidasYConTope() = runBlocking {
        val remote = FakeRemote(RemoteLookup.Found(entry("x"))); val r = repo(remote)
        r.lookup("one"); r.lookup("two"); r.lookup("one")
        assertEquals(listOf("one", "two"), r.history.value)
        ('a'..'z').forEach { r.lookup("w$it".replace(Regex("[0-9]"), "")) }
        assertEquals(20, r.history.value.size)
    }

    @Test
    fun historial_seRecuperaAlReabrir() {
        val store = FakeStore(history = listOf("seven", "one"))
        assertEquals(listOf("seven", "one"), repo(FakeRemote(RemoteLookup.NotFound), store).history.value)
    }

    @Test
    fun quitarUnaPalabra_borraSuHistorialYSuCache() = runBlocking {
        val store = FakeStore(); val r = repo(FakeRemote(RemoteLookup.Found(entry("x"))), store)
        r.lookup("one"); r.lookup("two")
        r.removeFromHistory("one")
        assertEquals(listOf("two"), r.history.value)
        assertEquals(listOf("two"), store.history)
        assertNull(store.entries["one"]); assertFalse(store.entries["two"] == null)
    }

    @Test
    fun borrarTodo() = runBlocking {
        val store = FakeStore(); val r = repo(FakeRemote(RemoteLookup.Found(entry("x"))), store)
        r.lookup("one"); r.lookup("two"); r.clearHistory()
        assertTrue(r.history.value.isEmpty()); assertTrue(store.history.isEmpty()); assertTrue(store.entries.isEmpty())
    }

    // ---------- Traducción al español ----------

    @Test
    fun traduce_laPalabraLasDefinicionesYLosEjemplos() = runBlocking {
        val translator = FakeTranslator(); val store = FakeStore()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store, translator = translator)
        val result = r.translateToSpanish("seven", full("seven"))!!
        assertEquals("ES(seven)", result.word)
        assertEquals("Numeral", result.meanings[0].partOfSpeech)                    // la categoría ya viene en español
        assertEquals("ES(def one)", result.meanings[0].definitions[0].text)
        assertEquals("ES(example one)", result.meanings[0].definitions[0].example)
        assertEquals("ES(def two)", result.meanings[0].definitions[1].text)
        assertNull(result.meanings[0].definitions[1].example)
    }

    @Test
    fun traduccion_seGuardaYLaSegundaVezNoVuelveATraducir() = runBlocking {
        val translator = FakeTranslator(); val store = FakeStore()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store, translator = translator)
        val first = r.translateToSpanish("seven", full("seven"))
        val callsAfterFirst = translator.translated.size
        val second = r.translateToSpanish("seven", full("seven"))
        assertEquals(first, second)
        assertEquals(callsAfterFirst, translator.translated.size)                  // no tradujo de nuevo
        assertEquals(first, store.translations["seven"])
    }

    @Test
    fun traduccionGuardada_funcionaAunqueElTraductorNoEsteDisponible() = runBlocking {
        val store = FakeStore().apply { translations["seven"] = TranslatedEntry("siete", emptyList()) }
        val offline = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store, translator = FakeTranslator(prepared = false))
        assertEquals("siete", offline.translateToSpanish("seven", full("seven"))!!.word)
        val withoutTranslator = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store)
        assertEquals("siete", withoutTranslator.translateToSpanish("seven", full("seven"))!!.word)
    }

    @Test
    fun sinTraductor_oSinIdiomaDescargado_devuelveNull() = runBlocking {
        assertNull(repo(FakeRemote(RemoteLookup.NotFound)).translateToSpanish("seven", full("seven")))
        val notReady = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), translator = FakeTranslator(prepared = false))
        assertNull(notReady.translateToSpanish("seven", full("seven")))
    }

    @Test
    fun siFallaLaTraduccionDeLaPalabra_devuelveNull() = runBlocking {
        val translator = FakeTranslator().apply { failOn += "seven" }
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), translator = translator)
        assertNull(r.translateToSpanish("seven", full("seven")))
    }

    @Test
    fun siFallaUnaFrase_seDejaEnInglesYNoSeGuardaParaCompletarlaDespues() = runBlocking {
        val translator = FakeTranslator().apply { failOn += "def two" }; val store = FakeStore()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store, translator = translator)
        val result = r.translateToSpanish("seven", full("seven"))!!
        assertEquals("ES(def one)", result.meanings[0].definitions[0].text)
        assertEquals("def two", result.meanings[0].definitions[1].text)             // quedó en inglés
        assertTrue(store.translations.isEmpty())                                    // no se guardó incompleta
    }

    @Test
    fun laTraduccionSeGuardaConLaMismaLlaveQueLaBusqueda() = runBlocking {
        val store = FakeStore(); val translator = FakeTranslator()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), store, translator = translator)
        r.translateToSpanish("  Seven ", full("Seven"))
        assertTrue(store.translations.containsKey("seven"))
    }

    @Test
    fun quitarUnaPalabraDelHistorial_tambienBorraSuTraduccion() = runBlocking {
        val store = FakeStore(); val translator = FakeTranslator()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.Found(full("seven"))), store, translator = translator)
        r.lookup("seven"); r.translateToSpanish("seven", full("seven"))
        assertTrue(store.translations.containsKey("seven"))
        r.removeFromHistory("seven")
        assertTrue(store.translations.isEmpty()); assertTrue(store.entries.isEmpty())
    }

    @Test
    fun calentarElTraductor_lePideQueSePrepare() = runBlocking {
        val translator = FakeTranslator()
        DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), translator = translator).warmUpTranslator()
        assertEquals(1, translator.prepareCalls)
        DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore()).warmUpTranslator()   // sin traductor: no pasa nada
    }

    // ---------- Audio de pronunciación ----------

    @Test
    fun audio_seDescargaUnaVezYLaSiguienteSaleDeLaCopiaLocal() = runBlocking {
        val audio = FakeAudio()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), pronunciation = audio)
        assertEquals("/audios/a.mp3", r.preparePronunciation(full("seven")))
        assertEquals("/audios/a.mp3", r.preparePronunciation(full("seven")))
        assertEquals(1, audio.downloads)
    }

    @Test
    fun audio_yaGuardadoNoSeDescargaDeNuevo() = runBlocking {
        val audio = FakeAudio().apply { saved["https://audio/seven.mp3"] = "/ya/estaba.mp3" }
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), pronunciation = audio)
        assertEquals("/ya/estaba.mp3", r.preparePronunciation(full("seven")))
        assertEquals(0, audio.downloads)
    }

    @Test
    fun audio_sinDireccionOSinCopiaLocal_devuelveNull() = runBlocking {
        val audio = FakeAudio()
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), pronunciation = audio)
        assertNull(r.preparePronunciation(WordEntry(word = "x", audioUrl = null)))
        assertEquals(0, audio.downloads)
        assertNull(repo(FakeRemote(RemoteLookup.NotFound)).preparePronunciation(full("seven")))
    }

    @Test
    fun audio_siFallaLaDescarga_devuelveNull() = runBlocking {
        val r = DictionaryRepository(FakeRemote(RemoteLookup.NotFound), FakeStore(), pronunciation = FakeAudio(downloadResult = null))
        assertNull(r.preparePronunciation(full("seven")))
    }
}
