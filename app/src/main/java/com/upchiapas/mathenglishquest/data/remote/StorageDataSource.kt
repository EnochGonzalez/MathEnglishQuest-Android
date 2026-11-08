package com.upchiapas.mathenglishquest.data.remote

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.io.File

/**
 * Fotos del portafolio en **Firebase Storage**.
 *
 * La app no usa las URLs públicas: sube el archivo comprimido y luego se lo descarga a su
 * propia carpeta privada. Así la pantalla sigue mostrando los `File` de siempre y las fotos
 * ya descargadas se ven sin internet.
 */
class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    /**
     * Sube una foto y devuelve su ruta dentro del bucket (no una URL).
     * Si se vuelve a subir el mismo nombre, se reemplaza (no se duplica).
     */
    suspend fun subirEvidencia(uid: String, archivo: File, nombreArchivo: String): String {
        val ruta = "${FirestoreSchema.CARPETA_EVIDENCIAS}/$uid/$nombreArchivo"
        val metadata = StorageMetadata.Builder()
            .setContentType("image/jpeg")
            .build()
        storage.reference.child(ruta)
            .putFile(Uri.fromFile(archivo), metadata)
            .awaitTask()
        return ruta
    }

    /** Descarga una foto a [destino]. Devuelve false si no se pudo (sin red, borrada, etc.). */
    suspend fun descargar(rutaStorage: String, destino: File): Boolean = try {
        destino.parentFile?.mkdirs()
        storage.reference.child(rutaStorage).getFile(destino).awaitTask()
        true
    } catch (e: Exception) {
        destino.delete()
        false
    }
}
