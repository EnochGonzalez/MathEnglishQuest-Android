package com.upchiapas.mathenglishquest.data.remote.dictionary

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Arma el cliente de Retrofit (dirección base, tiempos de espera y conversión de JSON con Gson). */
object DictionaryClient {
    private const val BASE_URL = "https://api.dictionaryapi.dev/"

    /** Cliente HTTP compartido: lo usan la API del diccionario y la descarga de los audios. */
    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun createApi(): DictionaryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DictionaryApi::class.java)
    }
}
