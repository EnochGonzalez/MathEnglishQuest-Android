package com.upchiapas.mathenglishquest.data.remote.dictionary

import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class RetrofitDictionaryRemoteTest {

    private fun remoteThatThrows(error: Throwable) = RetrofitDictionaryRemote(object : DictionaryApi {
        override suspend fun lookup(word: String): List<EntryDto> = throw error
    })

    private fun httpError(code: Int) = HttpException(Response.error<List<EntryDto>>(code, "".toResponseBody()))

    @Test
    fun respuestaCorrecta_seConvierteEnPalabra() = runBlocking {
        val api = object : DictionaryApi {
            override suspend fun lookup(word: String) = listOf(
                EntryDto(word = word, phonetic = "/x/", meanings = listOf(MeaningDto("noun", listOf(DefinitionDto("algo")))))
            )
        }
        val result = RetrofitDictionaryRemote(api).lookup("seven")
        assertTrue(result is RemoteLookup.Found)
        assertEquals("seven", (result as RemoteLookup.Found).entry.word)
    }

    @Test
    fun http404_esPalabraInexistente() = runBlocking {
        assertEquals(RemoteLookup.NotFound, remoteThatThrows(httpError(404)).lookup("zzzz"))
    }

    @Test
    fun otrosHttp_sonErrorDelServidorConSuCodigo() = runBlocking {
        assertEquals(RemoteLookup.ServerError(429), remoteThatThrows(httpError(429)).lookup("a"))
        assertEquals(RemoteLookup.ServerError(503), remoteThatThrows(httpError(503)).lookup("a"))
    }

    @Test
    fun sinInternetOTiempoAgotado_esSinConexion() = runBlocking {
        assertEquals(RemoteLookup.NoConnection, remoteThatThrows(UnknownHostException("api.dictionaryapi.dev")).lookup("a"))
        assertEquals(RemoteLookup.NoConnection, remoteThatThrows(SocketTimeoutException("timeout")).lookup("a"))
        assertEquals(RemoteLookup.NoConnection, remoteThatThrows(IOException("reset")).lookup("a"))
    }

    @Test
    fun respuestaRara_noTumbaLaApp() = runBlocking {
        assertEquals(RemoteLookup.ServerError(null), remoteThatThrows(IllegalStateException("JSON mal formado")).lookup("a"))
    }

    @Test
    fun respuestaVaciaOSinDatosUtiles_esInexistente() = runBlocking {
        val api = object : DictionaryApi { override suspend fun lookup(word: String) = emptyList<EntryDto>() }
        assertEquals(RemoteLookup.NotFound, RetrofitDictionaryRemote(api).lookup("a"))
    }

    @Test(expected = CancellationException::class)
    fun cancelarNoSeTragaComoError() {
        runBlocking { remoteThatThrows(CancellationException("cancelada")).lookup("a") }
    }
}
