package com.upchiapas.mathenglishquest.integration.qr

/**
 * Lo que va escrito dentro del QR de una clase y cómo se lee de vuelta. Es lógica pura (sin Android),
 * así se puede probar sin celular.
 *
 * El QR dice, por ejemplo, `MEQ-CLASE:AB3C7D`. El prefijo permite saber que es de esta app. Al leer
 * también se acepta el código a secas ("AB3C7D") por si el maestro generó el QR con otra herramienta.
 */
object ClassQrPayload {
    private const val PREFIX = "MEQ-CLASE:"

    /** Mismas letras y números que usa el registro del maestro para crear códigos (sin I, O, 0 ni 1). */
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    /** Los códigos de clase tienen 6 caracteres; 8 si hubo choques al crearlos. */
    private val CODE = Regex("^[$ALPHABET]{6,8}$")

    fun encode(classCode: String): String = PREFIX + classCode.trim().uppercase()

    /** El código de clase que trae el texto leído del QR, o null si no es de una clase. */
    fun decode(rawValue: String?): String? {
        val text = rawValue?.trim()?.uppercase() ?: return null
        val code = if (text.startsWith(PREFIX)) text.removePrefix(PREFIX) else text
        return code.takeIf { CODE.matches(it) }
    }
}
