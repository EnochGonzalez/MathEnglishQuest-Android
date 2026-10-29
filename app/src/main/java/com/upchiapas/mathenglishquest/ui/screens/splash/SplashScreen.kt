package com.upchiapas.mathenglishquest.ui.screens.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.components.DiceLogo
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import com.upchiapas.mathenglishquest.utils.rememberSoundPlayer

private val Purple = Color(0xFF8B5CF6)
private val PurpleLight = Color(0xFFC084FC)

@Composable
fun SplashScreen(
    onFinished: (SplashDestination) -> Unit,
    viewModel: SplashViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()
    val soundPlayer = rememberSoundPlayer()

    // Sonido de inicio: dura 2.8s y acompaña a la barra de carga
    LaunchedEffect(Unit) {
        soundPlayer.play(R.raw.splash_jingle)
    }

    LaunchedEffect(uiState.destination) {
        uiState.destination?.let(onFinished)
    }

    SplashContent()
}

@Composable
fun SplashContent() {
    val colors = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "splash")

    // @keyframes float: sube 15 y gira 5° y regresa (3s en total)
    val float by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "float"
    )
    // @keyframes glow: brillo morado que pulsa (2s, alternando)
    val glow by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    // @keyframes loadProgress: la barra crece de izquierda a derecha y luego se va por la derecha (3s)
    val bar by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "bar"
    )
    // @keyframes pulseText: el texto baja a 50% de opacidad y regresa
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(750, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        DiceLogo(
            modifier = Modifier
                .graphicsLayer {
                    translationY = -15.dp.toPx() * float
                    rotationZ = 5f * float
                }
                .drawBehind {
                    // Resplandor: 10 → 20 de extensión y opacidad 0.1 → 0.4
                    val alpha = 0.1f + 0.3f * glow
                    val spread = (10.dp + 10.dp * glow).toPx()
                    val corner = 16.dp.toPx()
                    for (i in 4 downTo 1) {
                        val s = spread * i / 4f
                        drawRoundRect(
                            color = Purple.copy(alpha = alpha * 0.22f),
                            topLeft = Offset(-s, -s),
                            size = Size(size.width + 2 * s, size.height + 2 * s),
                            cornerRadius = CornerRadius(corner + s)
                        )
                    }
                }
        )

        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Math & English Quest",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.5.sp,
            color = colors.ink
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            modifier = Modifier.fillMaxWidth(0.6f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.line)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val start = if (bar < 0.5f) 0f else (bar - 0.5f) * 2f
                    val end = if (bar < 0.5f) bar * 2f else 1f
                    if (end - start > 0.001f) {
                        val x0 = start * size.width
                        val x1 = end * size.width
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Purple, PurpleLight),
                                startX = x0,
                                endX = x1
                            ),
                            topLeft = Offset(x0, 0f),
                            size = Size(x1 - x0, size.height),
                            cornerRadius = CornerRadius(size.height / 2f)
                        )
                    }
                }
            }

            Text(
                text = "Cargando módulos...",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.mute,
                modifier = Modifier
                    .padding(top = 15.dp)
                    .alpha(pulse)
            )
        }
    }
}
