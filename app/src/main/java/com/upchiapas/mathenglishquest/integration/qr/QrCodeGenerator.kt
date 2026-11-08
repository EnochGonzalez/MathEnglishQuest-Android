package com.upchiapas.mathenglishquest.integration.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

/** Convierte un texto en un QR con la librería ZXing. Todo ocurre dentro del celular. */
object QrCodeGenerator {

    fun generate(text: String): QrMatrix {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,   // aguanta un poco de suciedad o brillo
            EncodeHintType.MARGIN to 0                                   // el margen blanco lo pone quien lo dibuja
        )
        val bits = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 0, 0, hints)
        return QrMatrix(
            size = bits.width,
            modules = BooleanArray(bits.width * bits.height) { index -> bits.get(index % bits.width, index / bits.width) }
        )
    }
}
