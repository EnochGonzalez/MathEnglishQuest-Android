package com.upchiapas.mathenglishquest.data.repository

import android.util.Log
import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Usuario
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.remote.FirebaseAuthDataSource
import com.upchiapas.mathenglishquest.data.remote.FirestoreDataSource
import com.upchiapas.mathenglishquest.data.remote.model.ClaseDoc
import com.upchiapas.mathenglishquest.data.remote.model.UsuarioDoc
import kotlin.random.Random

/** Usuario con sesión abierta, tal como lo necesita la interfaz. */
data class SessionUser(
    val name: String,
    val role: UserRole,
    val email: String?,
    val classCode: String?
) {
    val firstName: String get() = name.trim().substringBefore(' ')
}

/** Motivos por los que puede fallar entrar o registrarse. La pantalla decide el texto. */
enum class AuthError {
    CAMPOS_VACIOS,
    NOMBRE_INCOMPLETO,
    CORREO_INVALIDO,
    PASSWORD_CORTA,
    CORREO_EXISTENTE,
    CODIGO_INEXISTENTE,
    CREDENCIALES_INCORRECTAS,
    SIN_CONEXION,
    DESCONOCIDO
}

sealed interface AuthResult {
    data class Success(val user: SessionUser) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/**
 * Entrar y registrarse.
 *
 * La cuenta vive en **Firebase Authentication** (que guarda la contraseña cifrada) y el
 * perfil en **Firestore** (`usuarios/{uid}`). En el teléfono se guarda una copia en Room
 * ([UsuarioMirror]) para que el resto de la app siga funcionando con ids `Int` y sin internet.
 */
class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager,
    private val firebaseAuth: FirebaseAuthDataSource,
    private val firestore: FirestoreDataSource,
    private val mirror: UsuarioMirror
) {

    // ---------- Sesión ----------

    /** True si hay un invitado, o una cuenta que todavía existe en Firestore. */
    suspend fun hasValidSession(): Boolean = currentUser() != null

    suspend fun currentUser(): SessionUser? {
        if (session.isGuest()) {
            return SessionUser(name = "Invitado", role = UserRole.INVITADO, email = null, classCode = null)
        }

        session.userId()?.let { id ->
            usuarioDao.buscarPorId(id)?.let { return it.toSessionUser() }
        }

        // La sesión de Firebase puede seguir abierta aunque la copia local se haya borrado
        // (por ejemplo, si limpiaron los datos de la app). En ese caso se recupera el perfil.
        val uid = firebaseAuth.uidActual ?: return null
        val perfil = runCatching { firestore.buscarUsuario(uid) }.getOrNull() ?: return null
        val localId = runCatching { mirror.guardar(perfil) }.getOrNull() ?: return null
        session.saveUser(localId)
        return perfil.toSessionUser()
    }

    fun continueAsGuest() {
        firebaseAuth.cerrarSesion()
        session.saveGuest()
    }

    fun logout() {
        firebaseAuth.cerrarSesion()
        session.clear()
    }

    // ---------- Login ----------

    suspend fun login(email: String, password: String): AuthResult {
        val correo = email.trim().lowercase()
        if (correo.isEmpty() || password.isEmpty()) return AuthResult.Failure(AuthError.CAMPOS_VACIOS)

        val uid = try {
            firebaseAuth.iniciarSesion(correo, password).uid
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        }

        val perfil = try {
            firestore.buscarUsuario(uid)
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        } ?: return AuthResult.Failure(AuthError.DESCONOCIDO)

        val localId = runCatching { mirror.guardar(perfil) }.getOrNull()
            ?: return AuthResult.Failure(AuthError.DESCONOCIDO)

        session.saveUser(localId)
        return AuthResult.Success(perfil.toSessionUser())
    }

    // ---------- Registro ----------

    suspend fun registerStudent(name: String, email: String, password: String, classCode: String): AuthResult {
        val nombre = name.trim()
        val correo = email.trim().lowercase()
        val codigo = classCode.trim().uppercase()

        validateCommon(nombre, correo, password, extraEmpty = codigo.isEmpty())?.let {
            return AuthResult.Failure(it)
        }

        // El código se valida ANTES de crear la cuenta, para no ocupar el correo si se equivocó.
        val clase = try {
            firestore.buscarClase(codigo)
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        } ?: return AuthResult.Failure(AuthError.CODIGO_INEXISTENTE)

        val uid = try {
            firebaseAuth.registrar(correo, password).uid
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        }

        val perfil = UsuarioDoc(
            uid = uid,
            nombreCompleto = nombre,
            correo = correo,
            rol = UserRole.ESTUDIANTE.dbValue,
            claseId = clase.codigo
        )

        return try {
            firestore.guardarUsuario(perfil)
            val localId = mirror.guardar(perfil)
            session.saveUser(localId)
            AuthResult.Success(perfil.toSessionUser())
        } catch (e: Exception) {
            // Si Firestore falla, se borra la cuenta recién creada para no dejar el correo ocupado.
            runCatching { firebaseAuth.eliminarCuentaActual() }
            AuthResult.Failure(e.toAuthError())
        }
    }

    suspend fun registerTeacher(name: String, email: String, password: String): AuthResult {
        val nombre = name.trim()
        val correo = email.trim().lowercase()

        validateCommon(nombre, correo, password, extraEmpty = false)?.let {
            return AuthResult.Failure(it)
        }

        val codigo = try {
            generateUniqueClassCode()
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        }

        val uid = try {
            firebaseAuth.registrar(correo, password).uid
        } catch (e: Exception) {
            return AuthResult.Failure(e.toAuthError())
        }

        val perfil = UsuarioDoc(
            uid = uid,
            nombreCompleto = nombre,
            correo = correo,
            rol = UserRole.MAESTRO.dbValue,
            claseId = codigo
        )

        return try {
            firestore.crearClase(ClaseDoc(codigo = codigo, maestroUid = uid, maestroNombre = nombre))
            firestore.guardarUsuario(perfil)
            val localId = mirror.guardar(perfil)
            session.saveUser(localId)
            AuthResult.Success(perfil.toSessionUser())
        } catch (e: Exception) {
            // Se deshace todo para no dejar una clase o un correo a medias.
            runCatching { firestore.eliminarClase(codigo) }
            runCatching { firebaseAuth.eliminarCuentaActual() }
            AuthResult.Failure(e.toAuthError())
        }
    }

    // ---------- Internos ----------

    /** Traduce las excepciones de Firebase a los motivos que entiende la interfaz. */
    private fun Throwable.toAuthError(): AuthError = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthError.PASSWORD_CORTA
        is FirebaseAuthUserCollisionException -> AuthError.CORREO_EXISTENTE
        is FirebaseAuthInvalidCredentialsException -> AuthError.CREDENCIALES_INCORRECTAS
        is FirebaseAuthInvalidUserException -> AuthError.CREDENCIALES_INCORRECTAS
        is FirebaseNetworkException -> AuthError.SIN_CONEXION
        else -> {
            Log.w("AuthRepository", "Error de autenticación: ${javaClass.simpleName}: $message")
            AuthError.DESCONOCIDO
        }
    }

    private fun validateCommon(name: String, email: String, password: String, extraEmpty: Boolean): AuthError? = when {
        name.isEmpty() || email.isEmpty() || password.isEmpty() || extraEmpty -> AuthError.CAMPOS_VACIOS
        !hasFullName(name) -> AuthError.NOMBRE_INCOMPLETO
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> AuthError.CORREO_INVALIDO
        password.length < MIN_PASSWORD_LENGTH -> AuthError.PASSWORD_CORTA
        else -> null
    }

    /** Un registro válido exige nombre y dos apellidos (mínimo 3 palabras), igual que el prototipo. */
    private fun hasFullName(name: String): Boolean =
        name.trim().split(Regex("\\s+")).count { it.isNotEmpty() } >= 3

    /**
     * Código de 6 caracteres sin letras ni números que se confunden (0/O, 1/I).
     * Como el código es el id del documento en `clases`, basta leerlo para saber si está ocupado.
     */
    private suspend fun generateUniqueClassCode(): String {
        repeat(20) {
            val code = randomCode(6)
            if (firestore.buscarClase(code) == null) return code
        }
        return randomCode(8)
    }

    private fun randomCode(length: Int): String =
        (1..length).map { CODE_CHARS[Random.nextInt(CODE_CHARS.length)] }.joinToString("")

    private fun Usuario.toSessionUser() = SessionUser(
        name = nombreCompleto,
        role = UserRole.fromDb(rol),
        email = correo,
        classCode = claseId
    )

    private fun UsuarioDoc.toSessionUser() = SessionUser(
        name = nombreCompleto,
        role = UserRole.fromDb(rol),
        email = correo.ifBlank { null },
        classCode = claseId
    )

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
