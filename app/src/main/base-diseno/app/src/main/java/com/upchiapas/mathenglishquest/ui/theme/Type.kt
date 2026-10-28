package com.upchiapas.mathenglishquest.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R

/** Nunito, igual que el prototipo (pesos 600, 700, 800 y 900). */
val Nunito = FontFamily(
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
    Font(R.font.nunito_black, FontWeight.Black),
)

private val base = Typography()

val Typography = Typography(
    displayLarge = base.displayLarge.copy(fontFamily = Nunito),
    displayMedium = base.displayMedium.copy(fontFamily = Nunito),
    displaySmall = base.displaySmall.copy(fontFamily = Nunito),
    headlineLarge = base.headlineLarge.copy(fontFamily = Nunito),
    headlineMedium = base.headlineMedium.copy(fontFamily = Nunito),
    headlineSmall = base.headlineSmall.copy(fontFamily = Nunito),
    titleLarge = base.titleLarge.copy(fontFamily = Nunito),
    titleMedium = base.titleMedium.copy(fontFamily = Nunito),
    titleSmall = base.titleSmall.copy(fontFamily = Nunito),
    bodyLarge = base.bodyLarge.copy(fontFamily = Nunito),
    bodyMedium = base.bodyMedium.copy(fontFamily = Nunito),
    bodySmall = base.bodySmall.copy(fontFamily = Nunito),
    labelLarge = base.labelLarge.copy(fontFamily = Nunito),
    labelMedium = base.labelMedium.copy(fontFamily = Nunito),
    labelSmall = base.labelSmall.copy(fontFamily = Nunito),
)

/** Estilos con las medidas del prototipo (h1.page-title, p.page-sub, .auth-label, .nav-btn...). */
object AppText {
    val pageTitle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
    val topBarTitle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
    val pageSub = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 19.sp)
    val label = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
    val button = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 16.5.sp)
    val navLabel = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
    val equation = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 23.sp)
}
