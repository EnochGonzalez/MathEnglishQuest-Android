package com.upchiapas.mathenglishquest.data.repository

import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryLocalStore
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryRemote
import com.upchiapas.mathenglishquest.domain.dictionary.LookupResult
import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
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
        override fun loadHistory() = history
        override fun saveHistory(words: List<String>) { history = words }
        override fun loadEntry(word: String) = entries[word]
        override fun saveEntry(word: String, entry: WordEntry) { entries[word] = entry }
        override fun removeEntry(word: String) { entries.remove(word) }
    }

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
}
