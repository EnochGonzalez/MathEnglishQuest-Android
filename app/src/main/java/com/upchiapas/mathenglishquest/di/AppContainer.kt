package com.upchiapas.mathenglishquest.di

import android.content.Context
import com.upchiapas.mathenglishquest.data.local.DatabaseProvider
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import com.upchiapas.mathenglishquest.data.repository.DadoProgressRepository
import com.upchiapas.mathenglishquest.data.repository.EvidenceRepository

/**
 * Crea y comparte los objetos de la app (base de datos, sesión, repositorios).
 * Es una "inyección de dependencias" a mano: sencilla y sin librerías extra.
 */
class AppContainer(context: Context) {
    private val database = DatabaseProvider.getDatabase(context)
    private val sessionManager = SessionManager(context)

    val authRepository = AuthRepository(database.usuarioDao(), sessionManager)
    val dadoProgressRepository = DadoProgressRepository(context)
    val evidenceRepository = EvidenceRepository(context, database.evidenciaDao(), sessionManager)

    companion object {
        @Volatile private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
    }
}
