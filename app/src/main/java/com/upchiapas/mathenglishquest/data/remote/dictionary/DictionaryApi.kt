package com.upchiapas.mathenglishquest.data.remote.dictionary

import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API externa: Free Dictionary API (https://dictionaryapi.dev). Es pública y no necesita llave.
 * Ejemplo: GET https://api.dictionaryapi.dev/api/v2/entries/en/fourteen
 * Si la palabra no existe responde 404.
 */
interface DictionaryApi {
    @GET("api/v2/entries/en/{word}")
    suspend fun lookup(@Path("word") word: String): List<EntryDto>
}
