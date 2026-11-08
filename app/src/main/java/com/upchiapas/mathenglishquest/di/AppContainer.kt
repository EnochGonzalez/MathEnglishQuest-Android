package com.upchiapas.mathenglishquest.di

import android.content.Context
import com.upchiapas.mathenglishquest.data.local.DatabaseProvider
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.remote.FirebaseAuthDataSource
import com.upchiapas.mathenglishquest.data.remote.FirestoreDataSource
import com.upchiapas.mathenglishquest.data.remote.StorageDataSource
import com.upchiapas.mathenglishquest.data.repository.AuthRepository
import com.upchiapas.mathenglishquest.data.repository.ClassRepository
import com.upchiapas.mathenglishquest.data.repository.DadoProgressRepository
import com.upchiapas.mathenglishquest.data.repository.EvidenceRepository
import com.upchiapas.mathenglishquest.data.repository.UsuarioMirror
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Crea y comparte los objetos de la app (base de datos, sesión, Firebase, repositorios).
 * Es una "inyección de dependencias" a mano: sencilla y sin librerías extra.
 */
class AppContainer(context: Context) {
    private val database = DatabaseProvider.getDatabase(context)
    private val sessionManager = SessionManager(context)

    // ---------- Nube ----------
    private val firebaseAuth = FirebaseAuthDataSource()
    private val firestore = FirestoreDataSource()
    private val storage = StorageDataSource()

    /** Copia local de los perfiles que vienen de Firestore. */
    private val usuarioMirror = UsuarioMirror(database.usuarioDao())

    /** Para escuchar cambios de la nube sin atarlos a la pantalla que los pidió. */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ---------- Repositorios ----------
    val authRepository = AuthRepository(
        usuarioDao = database.usuarioDao(),
        session = sessionManager,
        firebaseAuth = firebaseAuth,
        firestore = firestore,
        mirror = usuarioMirror
    )
    val dadoProgressRepository = DadoProgressRepository(context, firebaseAuth, firestore, appScope)
    val evidenceRepository = EvidenceRepository(
        context = context,
        evidenciaDao = database.evidenciaDao(),
        usuarioDao = database.usuarioDao(),
        session = sessionManager,
        firestore = firestore,
        storage = storage,
        mirror = usuarioMirror,
        scope = appScope
    )
    val classRepository = ClassRepository(
        usuarioDao = database.usuarioDao(),
        session = sessionManager,
        evidenceRepository = evidenceRepository,
        firestore = firestore,
        mirror = usuarioMirror,
        scope = appScope
    )

    companion object {
        @Volatile private var INSTANCE: AppContainer? = null

        fun getInstance(context: Context): AppContainer =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppContainer(context.applicationContext).also { INSTANCE = it }
            }
    }
}
