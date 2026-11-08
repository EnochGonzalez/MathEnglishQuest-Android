package com.upchiapas.mathenglishquest.integration.qr

/** Un QR ya calculado: una cuadrícula de [size] x [size] módulos, cada uno oscuro (true) o claro (false). */
class QrMatrix(val size: Int, private val modules: BooleanArray) {
    init {
        require(modules.size == size * size) { "Se esperaban ${size * size} módulos y llegaron ${modules.size}" }
    }

    /** Fuera de la cuadrícula siempre es claro, así quien dibuja no se sale de los límites. */
    operator fun get(x: Int, y: Int): Boolean =
        x in 0 until size && y in 0 until size && modules[y * size + x]
}
