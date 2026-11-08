package com.upchiapas.mathenglishquest.data.remote.dictionary

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Arma el cliente de Retrofit (dirección base, tiempos de espera y conversión de JSON con Gson). */
object DictionaryClient {
    private const val BASE_URL = "https://api.dictionaryapi.dev/"

    fun createApi(): DictionaryApi {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApi::class.java)
    }
}
