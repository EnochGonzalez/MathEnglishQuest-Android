package com.upchiapas.mathenglishquest.data.repository

import android.database.sqlite.SQLiteConstraintException
import android.util.Patterns
import com.upchiapas.mathenglishquest.data.local.SessionManager
import com.upchiapas.mathenglishquest.data.local.dao.UsuarioDao
import com.upchiapas.mathenglishquest.data.local.entity.Usuario
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    DESCONOCIDO
}

sealed interface AuthResult {
    data class Success(val user: SessionUser) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

class AuthRepository(
    private val usuarioDao: UsuarioDao,
    private val session: SessionManager
) {

    // ---------- Sesión ----------

    /** True si hay un invitado, o una cuenta que todavía existe en la base de datos. */
    suspend fun hasValidSession(): Boolean = currentUser() != null

    suspend fun currentUser(): SessionUser? {
        if (session.isGuest()) {
            return SessionUser(name = "Invitado", role = UserRole.INVITADO, email = null, classCode = null)
        }
        val id = session.userId() ?: return null
        return usuarioDao.buscarPorId(id)?.toSessionUser()
    }

    fun continueAsGuest() = session.saveGuest()

    fun logout() = session.clear()

    // ---------- Login ----------

    suspend fun login(email: String, password: String): AuthResult {
        val correo = email.trim().lowercase()
        if (correo.isEmpty() || password.isEmpty()) return AuthResult.Failure(AuthError.CAMPOS_VACIOS)

        val usuario = usuarioDao.buscarPorCorreo(correo)
        // Se verifica siempre con el hash, aunque el correo no exista, para no revelar cuáles cuentas existen.
        val ok = withContext(Dispatchers.Default) {
            usuario != null && PasswordHasher.verify(password, usuario.passwordHash)
        }
        if (usuario == null || !ok) return AuthResult.Failure(AuthError.CREDENCIALES_INCORRECTAS)

        session.saveUser(usuario.id)
        return AuthResult.Success(usuario.toSessionUser())
    }

    // ---------- Registro ----------

    suspend fun registerStudent(name: String, email: String, password: String, classCode: String): AuthResult {
        val nombre = name.trim()
        val correo = email.trim().lowercase()
        val codigo = classCode.trim().uppercase()

        validateCommon(nombre, correo, password, extraEmpty = codigo.isEmpty())?.let {
            return AuthResult.Failure(it)
        }
        if (usuarioDao.buscarPorCorreo(correo) != null) return AuthResult.Failure(AuthError.CORREO_EXISTENTE)
        if (usuarioDao.buscarMaestroPorCodigo(codigo) == null) return AuthResult.Failure(AuthError.CODIGO_INEXISTENTE)

        return insertUser(nombre, correo, password, UserRole.ESTUDIANTE, codigo)
    }

    suspend fun registerTeacher(name: String, email: String, password: String): AuthResult {
        val nombre = name.trim()
        val correo = email.trim().lowercase()

        validateCommon(nombre, correo, password, extraEmpty = false)?.let {
            return AuthResult.Failure(it)
        }
        if (usuarioDao.buscarPorCorreo(correo) != null) return AuthResult.Failure(AuthError.CORREO_EXISTENTE)

        return insertUser(nombre, correo, password, UserRole.MAESTRO, generateUniqueClassCode())
    }

    // ---------- Internos ----------

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

    private suspend fun insertUser(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        classCode: String
    ): AuthResult {
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(password) }
        val usuario = Usuario(
            nombreCompleto = name,
            correo = email,
            passwordHash = hash,
            rol = role.dbValue,
            claseId = classCode
        )
        return try {
            val id = usuarioDao.insertar(usuario).toInt()
            session.saveUser(id)
            AuthResult.Success(usuario.copy(id = id).toSessionUser())
        } catch (_: SQLiteConstraintException) {
            AuthResult.Failure(AuthError.CORREO_EXISTENTE)
        } catch (_: Exception) {
            AuthResult.Failure(AuthError.DESCONOCIDO)
        }
    }

    /** Código de 6 caracteres sin letras ni números que se confunden (0/O, 1/I), como en el prototipo. */
    private suspend fun generateUniqueClassCode(): String {
        repeat(20) {
            val code = (1..6).map { CODE_CHARS[Random.nextInt(CODE_CHARS.length)] }.joinToString("")
            if (usuarioDao.buscarMaestroPorCodigo(code) == null) return code
        }
        // Prácticamente imposible: se agrega un sufijo extra para no repetir.
        return (1..8).map { CODE_CHARS[Random.nextInt(CODE_CHARS.length)] }.joinToString("")
    }

    private fun Usuario.toSessionUser() = SessionUser(
        name = nombreCompleto,
        role = UserRole.fromDb(rol),
        email = correo,
        classCode = claseId
    )

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        const val CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    }
}
