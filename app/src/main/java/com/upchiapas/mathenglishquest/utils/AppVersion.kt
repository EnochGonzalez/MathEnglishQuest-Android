package com.upchiapas.mathenglishquest.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Versión de la app tal como está en `versionName` de app/build.gradle.kts.
 * Se lee del paquete instalado, así que al subir de versión solo se cambia en Gradle.
 */
@Composable
fun rememberAppVersion(): String {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }
}
