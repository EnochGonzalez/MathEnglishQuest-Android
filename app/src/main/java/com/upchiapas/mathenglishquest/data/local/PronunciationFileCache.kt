package com.upchiapas.mathenglishquest.data.local

import android.content.Context
import android.util.Log
import com.upchiapas.mathenglishquest.domain.dictionary.PronunciationCache
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest

/**
 * Guarda en una carpeta de la app los audios de pronunciación que manda el diccionario.
 * Así el segundo toque (y los siguientes, aunque no haya internet) suena al instante.
 */
class PronunciationFileCache(
    context: Context,
    private val httpClient: OkHttpClient
) : PronunciationCache {

    private val directory = File(context.applicationContext.filesDir, "pronunciaciones")

    override fun cachedPath(url: String): String? =
        fileFor(url).takeIf { it.exists() && it.length() > 0 }?.absolutePath

    override suspend fun download(url: String): String? = withContext(Dispatchers.IO) {
        val target = fileFor(url)
        val temp = File(target.parentFile, target.name + ".tmp")
        try {
            directory.mkdirs()
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) {
                    Log.w(TAG, "No se pudo bajar el audio ($url): HTTP ${response.code}")
                    return@withContext null
                }
                body.byteStream().use { input -> temp.outputStream().use { output -> input.copyTo(output) } }
            }
            if (temp.length() == 0L) {
                temp.delete()
                return@withContext null
            }
            // Se escribe a un archivo temporal y se renombra: nunca queda un audio a medias
            if (!temp.renameTo(target)) {
                temp.delete()
                return@withContext null
            }
            Log.d(TAG, "Audio guardado: ${target.name} (${target.length()} bytes)")
            target.absolutePath
        } catch (e: CancellationException) {
            temp.delete()
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Falló la descarga del audio ($url): ${e.javaClass.simpleName}: ${e.message}")
            temp.delete()
            null
        }
    }

    /** El nombre del archivo sale de la dirección (así no hay caracteres raros ni choques). */
    private fun fileFor(url: String): File {
        val hash = MessageDigest.getInstance("SHA-256").digest(url.toByteArray())
            .joinToString("") { "%02x".format(it) }
        return File(directory, "$hash.mp3")
    }

    private companion object {
        const val TAG = "PronunciationCache"
    }
}
