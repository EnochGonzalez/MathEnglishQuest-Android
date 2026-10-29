package com.upchiapas.mathenglishquest.data.security

import android.os.Build
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Hash de contraseñas con PBKDF2 y sal aleatoria. Se guarda como "algoritmo$iteraciones$sal$hash".
 * La contraseña original nunca se almacena. (Cuando entre Firebase Auth, esto se reemplaza.)
 */
object PasswordHasher {
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256
    private const val ITERATIONS = 100_000

    fun hash(password: String): String {
        val algorithm = defaultAlgorithm()
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val derived = derive(password, salt, algorithm, ITERATIONS)
        return listOf(algorithm, ITERATIONS.toString(), encode(salt), encode(derived)).joinToString("$")
    }

    fun verify(password: String, stored: String): Boolean {
        val parts = stored.split("$")
        if (parts.size != 4) return false
        val iterations = parts[1].toIntOrNull() ?: return false
        return try {
            val salt = Base64.decode(parts[2], Base64.NO_WRAP)
            val expected = Base64.decode(parts[3], Base64.NO_WRAP)
            val actual = derive(password, salt, parts[0], iterations)
            MessageDigest.isEqual(expected, actual)
        } catch (_: Exception) {
            false
        }
    }

    // PBKDF2WithHmacSHA256 existe desde Android 8 (API 26); en 7.x se usa SHA1.
    private fun defaultAlgorithm(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) "PBKDF2WithHmacSHA256" else "PBKDF2WithHmacSHA1"

    private fun derive(password: String, salt: ByteArray, algorithm: String, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
    }

    private fun encode(bytes: ByteArray): String = Base64.encodeToString(bytes, Base64.NO_WRAP)
}
