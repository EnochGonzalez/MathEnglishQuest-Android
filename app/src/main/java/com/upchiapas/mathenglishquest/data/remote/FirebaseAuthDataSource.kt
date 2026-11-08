package com.upchiapas.mathenglishquest.data.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Todo el contacto con **Firebase Authentication** (correo y contraseña).
 *
 * Aquí ya no se guardan contraseñas: Auth las cifra y solo devuelve el UID del usuario.
 * El perfil (nombre, rol, clase) vive en Firestore; ver [FirestoreDataSource].
 */
class FirebaseAuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    /** UID de la sesión abierta, o null si nadie entró. */
    val uidActual: String? get() = auth.currentUser?.uid

    /** Correo de la sesión abierta, tal como lo conoce Auth. */
    val correoActual: String? get() = auth.currentUser?.email

    /**
     * Entra con una cuenta existente.
     * Lanza excepción de Firebase si las credenciales son incorrectas o no hay red.
     */
    suspend fun iniciarSesion(correo: String, password: String): FirebaseUser =
        auth.signInWithEmailAndPassword(correo, password).awaitTask().user
            ?: error("Firebase Auth no devolvió el usuario")

    /**
     * Crea la cuenta en Auth.
     * Lanza excepción si el correo ya existe, la contraseña es débil o no hay red.
     */
    suspend fun registrar(correo: String, password: String): FirebaseUser =
        auth.createUserWithEmailAndPassword(correo, password).awaitTask().user
            ?: error("Firebase Auth no devolvió el usuario")

    fun cerrarSesion() = auth.signOut()

    /**
     * Borra la cuenta de Auth. Se usa si el registro falla a medias (por ejemplo, la cuenta se
     * creó pero Firestore no respondió), para no dejar un correo "ocupado" sin perfil.
     */
    suspend fun eliminarCuentaActual() {
        auth.currentUser?.delete()?.awaitTask()
    }
}
