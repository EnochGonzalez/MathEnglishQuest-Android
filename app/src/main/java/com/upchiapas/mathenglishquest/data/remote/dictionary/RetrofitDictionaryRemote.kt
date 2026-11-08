package com.upchiapas.mathenglishquest.data.remote.dictionary

import android.util.Log
import com.upchiapas.mathenglishquest.domain.dictionary.DictionaryRemote
import com.upchiapas.mathenglishquest.domain.dictionary.RemoteLookup
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

/**
 * Llama a la API con Retrofit y traduce todo lo que pueda pasar (palabra inexistente, sin internet, servidor
 * caído, respuesta rara) a un [RemoteLookup]. Así el resto de la app nunca ve excepciones de red.
 */
class RetrofitDictionaryRemote(
    private val api: DictionaryApi
) : DictionaryRemote {

    override suspend fun lookup(word: String): RemoteLookup = try {
        val entry = DictionaryMapper.toWordEntry(api.lookup(word))
        if (entry != null) {
            Log.d(TAG, "GET $word -> encontrada (${entry.meanings.size} significados)")
            RemoteLookup.Found(entry)
        } else {
            Log.d(TAG, "GET $word -> respuesta sin datos útiles")
            RemoteLookup.NotFound
        }
    } catch (e: CancellationException) {
        throw e                                   // cancelar una búsqueda no es un error
    } catch (e: HttpException) {
        Log.w(TAG, "GET $word -> HTTP ${e.code()}")
        if (e.code() == 404) RemoteLookup.NotFound else RemoteLookup.ServerError(e.code())
    } catch (e: IOException) {
        Log.w(TAG, "GET $word -> sin conexión: ${e.javaClass.simpleName}")
        RemoteLookup.NoConnection
    } catch (e: Exception) {
        Log.e(TAG, "GET $word -> respuesta inesperada: ${e.javaClass.simpleName}: ${e.message}")
        RemoteLookup.ServerError(null)
    }

    private companion object {
        const val TAG = "DictionaryApi"
    }
}
