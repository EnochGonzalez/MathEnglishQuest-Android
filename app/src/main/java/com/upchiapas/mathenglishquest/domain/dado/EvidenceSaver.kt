package com.upchiapas.mathenglishquest.domain.dado

/**
 * Guarda la foto del procedimiento. [source] es la dirección (Uri) de la imagen elegida o tomada y
 * [exerciseText] el ejercicio que el estudiante estaba resolviendo, para que el maestro lo vea junto a la foto.
 * Es una interfaz para poder probar el ViewModel sin Android. Devuelve false si no se pudo guardar.
 */
interface EvidenceSaver {
    suspend fun save(source: String, difficultyName: String, categories: List<String>, exerciseText: String): Boolean
}
