package com.upchiapas.mathenglishquest.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Tokens de color del prototipo web (common.css), en versión clara y oscura.
 * Los nombres "deep" son los tonos de la sombra dura de los botones 3D.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,

    // Superficies y texto
    val background: Color,      // fondo de pantalla (--screen)
    val surface: Color,         // tarjetas, barra inferior, campos (#fff)
    val ink: Color,             // texto principal (--ink)
    val mute: Color,            // texto secundario (--mute)
    val line: Color,            // bordes de 2dp (--line)
    val lineStrong: Color,      // sombra dura de tarjetas (--line-strong)
    val inverseBg: Color,       // hero de Inicio, chip activo (fondo "oscuro")
    val onInverse: Color,       // texto sobre inverseBg

    // Marca
    val blue: Color, val blueDeep: Color, val blueText: Color,
    val purple: Color, val purpleDeep: Color,
    val green: Color, val greenDeep: Color,
    val red: Color, val redDeep: Color,
    val amber: Color,

    // Fondos suaves + texto para los íconos de módulo y las insignias
    val blueSoft: Color, val blueSoftText: Color,
    val purpleSoft: Color, val purpleSoftText: Color,
    val greenSoft: Color, val greenSoftText: Color,
    val redSoft: Color, val redSoftText: Color,
    val yellowSoft: Color, val yellowSoftText: Color,
)

val LightAppColors = AppColors(
    isDark = false,
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    ink = Color(0xFF1E293B),
    mute = Color(0xFF64748B),
    line = Color(0xFFE2E8F0),
    lineStrong = Color(0xFFCBD5E1),
    inverseBg = Color(0xFF1E293B),
    onInverse = Color(0xFFFFFFFF),

    blue = Color(0xFF3B82F6), blueDeep = Color(0xFF1D4ED8), blueText = Color(0xFF1D4ED8),
    purple = Color(0xFF8B5CF6), purpleDeep = Color(0xFF6D28D9),
    green = Color(0xFF22C55E), greenDeep = Color(0xFF16A34A),
    red = Color(0xFFEF4444), redDeep = Color(0xFFDC2626),
    amber = Color(0xFFF59E0B),

    blueSoft = Color(0xFFE0F2FE), blueSoftText = Color(0xFF0369A1),
    purpleSoft = Color(0xFFEDE9FE), purpleSoftText = Color(0xFF6D28D9),
    greenSoft = Color(0xFFDCFCE7), greenSoftText = Color(0xFF15803D),
    redSoft = Color(0xFFFEE2E2), redSoftText = Color(0xFFB91C1C),
    yellowSoft = Color(0xFFFEF9C3), yellowSoftText = Color(0xFFA16207),
)

/** Base tomada del logo oscuro (#1a1a1c / #222225 / #333336); lo demás es una adaptación. */
val DarkAppColors = AppColors(
    isDark = true,
    background = Color(0xFF1A1A1C),
    surface = Color(0xFF222225),
    ink = Color(0xFFF1F5F9),
    mute = Color(0xFFA1A1AA),
    line = Color(0xFF333336),
    lineStrong = Color(0xFF46464B),
    inverseBg = Color(0xFF2E2E33),
    onInverse = Color(0xFFFFFFFF),

    blue = Color(0xFF3B82F6), blueDeep = Color(0xFF1D4ED8), blueText = Color(0xFF93C5FD),
    purple = Color(0xFF8B5CF6), purpleDeep = Color(0xFF6D28D9),
    green = Color(0xFF22C55E), greenDeep = Color(0xFF16A34A),
    red = Color(0xFFEF4444), redDeep = Color(0xFFDC2626),
    amber = Color(0xFFF59E0B),

    blueSoft = Color(0xFF263146), blueSoftText = Color(0xFF7DD3FC),
    purpleSoft = Color(0xFF332B46), purpleSoftText = Color(0xFFC4B5FD),
    greenSoft = Color(0xFF223C2E), greenSoftText = Color(0xFF86EFAC),
    redSoft = Color(0xFF43282A), redSoftText = Color(0xFFFCA5A5),
    yellowSoft = Color(0xFF443621), yellowSoftText = Color(0xFFFDE047),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }
