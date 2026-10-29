package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** Pantalla "¡Listo!" con el código de clase que se le da a los estudiantes. */
@Composable
fun TeacherCodeScreen(
    classCode: String,
    onContinue: () -> Unit
) {
    val colors = AppTheme.colors
    val accent = colors.greenSoftText

    // La cuenta ya quedó creada: "atrás" lleva al inicio, no de vuelta al formulario.
    BackHandler(onBack = onContinue)

    AuthScreenContainer {
        AuthHero(title = "¡Listo!", logoRes = R.drawable.ic_check_circle, logoTint = accent)

        Text(
            text = "Este es tu código de clase. Compártelo con tus estudiantes para que se registren y aparezcan en tu panel.",
            style = AppText.pageSub,
            color = colors.mute,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // .class-code-box: borde punteado verde y letras muy espaciadas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.greenSoft)
                .drawBehind {
                    val strokeWidth = 2.dp.toPx()
                    drawRoundRect(
                        color = accent,
                        topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        cornerRadius = CornerRadius(16.dp.toPx()),
                        style = Stroke(
                            width = strokeWidth,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 6.dp.toPx()))
                        )
                    )
                }
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = classCode,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = accent
            )
        }

        AuthPrimaryButton(text = "Ir a mi panel", onClick = onContinue)
    }
}
