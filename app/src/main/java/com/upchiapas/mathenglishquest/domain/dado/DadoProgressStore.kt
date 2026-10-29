package com.upchiapas.mathenglishquest.domain.dado

import com.upchiapas.mathenglishquest.data.model.DadoProgress

/** Dónde se guarda el progreso del Dado. Es una interfaz para poder probar el ViewModel sin Android. */
interface DadoProgressStore {
    fun load(userKey: String): DadoProgress?
    fun save(userKey: String, progress: DadoProgress)
}
