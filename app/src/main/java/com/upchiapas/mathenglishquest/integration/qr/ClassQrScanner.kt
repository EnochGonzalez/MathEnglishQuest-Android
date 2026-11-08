package com.upchiapas.mathenglishquest.integration.qr

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

/** Cómo terminó un escaneo. */
sealed interface QrScanOutcome {
    /** Se leyó un QR. [rawValue] es el texto que traía. */
    data class Scanned(val rawValue: String?) : QrScanOutcome

    /** La persona cerró el lector sin escanear nada. */
    data object Canceled : QrScanOutcome

    /** No se pudo abrir o usar el lector (por ejemplo, Google Play Services no está disponible). */
    data class Failed(val message: String?) : QrScanOutcome
}

/**
 * Lector de QR de Google (Google Code Scanner). La cámara y la pantalla de escaneo las pone Google Play Services,
 * así que la app no necesita pedir permiso de cámara: solo recibe el texto del QR.
 */
class ClassQrScanner(private val context: Context) {

    fun scan(onOutcome: (QrScanOutcome) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()

        GmsBarcodeScanning.getClient(context, options)
            .startScan()
            .addOnSuccessListener { barcode -> onOutcome(QrScanOutcome.Scanned(barcode.rawValue)) }
            .addOnCanceledListener { onOutcome(QrScanOutcome.Canceled) }
            .addOnFailureListener { error -> onOutcome(QrScanOutcome.Failed(error.message)) }
    }
}

@Composable
fun rememberClassQrScanner(): ClassQrScanner {
    val context = LocalContext.current
    return remember(context) { ClassQrScanner(context) }
}
