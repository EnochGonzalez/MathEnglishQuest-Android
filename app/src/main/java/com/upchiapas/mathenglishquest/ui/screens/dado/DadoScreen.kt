package com.upchiapas.mathenglishquest.ui.screens.dado

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.data.model.GameMode
import com.upchiapas.mathenglishquest.domain.dado.NumberFormatter
import com.upchiapas.mathenglishquest.ui.components.AnswerButton
import com.upchiapas.mathenglishquest.ui.components.ChunkyButton
import com.upchiapas.mathenglishquest.ui.components.EquationCard
import com.upchiapas.mathenglishquest.ui.components.ModalDialog
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import com.upchiapas.mathenglishquest.utils.EvidenceFiles

@Composable
fun DadoScreen(
    viewModel: DadoViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Dirección donde la cámara deja la foto. Se guarda aparte por si Android cierra la app mientras se toma.
    var pendingCaptureUri by rememberSaveable { mutableStateOf<Uri?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (success && uri != null) viewModel.onEvidencePicked(uri.toString())
    }
    val pickPicture = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.onEvidencePicked(uri.toString())
    }

    DadoScreenContent(
        state = state,
        onTakePhoto = {
            try {
                val uri = EvidenceFiles.createCaptureUri(context)
                pendingCaptureUri = uri
                takePicture.launch(uri)
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo abrir la cámara en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        },
        onPickPhoto = {
            try {
                pickPicture.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: Exception) {
                Toast.makeText(context, "No se pudo abrir la galería en este dispositivo", Toast.LENGTH_SHORT).show()
            }
        },
        onModeSelected = viewModel::onModeSelected,
        onDifficultySelected = viewModel::onDifficultySelected,
        onNegativesChanged = viewModel::onNegativesChanged,
        onRollDice = viewModel::onRollDice,
        onWildcard = viewModel::onWildcard,
        onAnswerSelected = viewModel::onAnswerSelected,
        onHowToClick = viewModel::onHowToClick,
        onDialogDismissed = viewModel::onDialogDismissed,
        onGameOverDismissed = viewModel::onGameOverDismissed
    )
}

@Composable
fun DadoScreenContent(
    state: DadoUiState,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onDifficultySelected: (Difficulty) -> Unit,
    onNegativesChanged: (Boolean) -> Unit,
    onRollDice: () -> Unit,
    onWildcard: () -> Unit,
    onAnswerSelected: (Double) -> Unit,
    onHowToClick: () -> Unit,
    onDialogDismissed: () -> Unit,
    onGameOverDismissed: () -> Unit
) {
    val colors = AppTheme.colors

    DadoDialogs(
        dialog = state.dialog,
        state = state,
        onDismiss = onDialogDismissed,
        onGameOverDismissed = onGameOverDismissed
    )

    if (state.evidenceUploading) {
        ModalDialog(onDismissRequest = {}, dismissible = false) {
            CircularProgressIndicator(color = colors.purple)
            Spacer(modifier = Modifier.height(14.dp))
            ModalTitle("Subiendo tu foto…")
            ModalText("Espera un momento, no cierres esta pantalla ni vuelvas a tocar el botón de subir foto.")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        // ---- Barra superior: título, racha y corazones ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle(iconRes = R.drawable.ic_dice, title = "Módulo Dado")
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_star),
                        contentDescription = "Racha",
                        tint = colors.amber,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = state.game.streak.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.amber
                    )
                }
                Hearts(halfHearts = state.game.halfHearts)
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        // ---- Negativos ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.line)
                .padding(horizontal = 15.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_negative),
                    contentDescription = null,
                    tint = colors.mute,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Negativos",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.mute
                )
            }
            Switch(
                checked = state.game.allowNegatives,
                onCheckedChange = onNegativesChanged,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = colors.blue,
                    checkedBorderColor = Color.Transparent,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = colors.lineStrong,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ---- Modo ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            GameMode.entries.forEach { mode ->
                val selected = mode == state.game.mode
                ChunkyButton(
                    onClick = { onModeSelected(mode) },
                    modifier = Modifier.weight(1f),
                    containerColor = if (selected) colors.purple else colors.surface,
                    shadowColor = if (selected) colors.purpleDeep else Color.Transparent,
                    contentColor = if (selected) Color.White else colors.mute,
                    border = BorderStroke(2.dp, if (selected) colors.purple else colors.line),
                    depth = 3.dp,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 9.dp)
                ) {
                    Text(text = mode.title, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        // ---- Dificultad ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Difficulty.entries.forEach { difficulty ->
                val unlocked = difficulty.ordinal <= state.game.maxUnlockedLevel
                val active = difficulty == state.game.difficulty
                ChunkyButton(
                    onClick = { if (unlocked) onDifficultySelected(difficulty) },
                    modifier = Modifier.weight(1f),
                    containerColor = when {
                        active -> colors.blue
                        unlocked -> colors.blueSoft
                        else -> colors.lineStrong
                    },
                    shadowColor = if (active) colors.blueDeep else Color.Transparent,
                    contentColor = when {
                        active -> Color.White
                        unlocked -> colors.blueSoftText
                        else -> colors.mute
                    },
                    depth = 3.dp,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (difficulty == Difficulty.FACIL) difficulty.shortName
                        else "${difficulty.shortName}\n★${difficulty.unlockStreak}",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ---- Tablero: ecuación, reloj de Rush y respuestas (con scroll si no cabe) ----
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EquationCard(
                equation = when {
                    state.note is DadoNote.ChainDone -> "¡Secuencia completa!"
                    state.equation != null -> state.equation
                    else -> "Presiona Tirar Dado"
                },
                note = when (val note = state.note) {
                    DadoNote.None -> ""
                    DadoNote.Hierarchy ->
                        if (state.awaitingEvidence) EVIDENCE_NOTE
                        else "Resuelve respetando la jerarquía (paréntesis primero)"
                    is DadoNote.ChainStep ->
                        if (state.awaitingEvidence) EVIDENCE_NOTE
                        else "Paso ${note.current} de ${note.total}"
                    is DadoNote.ChainDone -> "Resultado final: ${note.finalResult}"
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            if (state.showTimer) {
                TimerBar(progress = state.timerProgress)
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (state.awaitingEvidence) {
                EvidenceGate(
                    showError = state.evidenceError,
                    onTakePhoto = onTakePhoto,
                    onPickPhoto = onPickPhoto
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    state.options.forEach { option ->
                        AnswerButton(
                            text = option.text,
                            isSelected = option.value == state.selectedValue,
                            isCorrect = option.value == state.correctValue,
                            isAnswered = state.isAnswered,
                            onClick = { onAnswerSelected(option.value) }
                        )
                    }
                }
            }
        }

        // ---- Botones de acción ----
        Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
            SoftActionButton(
                text = "¿Cómo se resuelve este ejercicio?",
                iconRes = R.drawable.ic_bulb,
                enabled = state.canOpenHowTo,
                onClick = onHowToClick,
                containerColor = colors.blueSoft,
                borderColor = colors.blue.copy(alpha = 0.3f),
                contentColor = colors.blueText,
                fontSize = 12.5.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            SoftActionButton(
                text = "Comodín: cambiar operación (−½ vida)",
                iconRes = R.drawable.ic_swap,
                enabled = state.canUseWildcard,
                onClick = onWildcard,
                containerColor = colors.amber.copy(alpha = 0.12f),
                borderColor = colors.amber.copy(alpha = 0.45f),
                contentColor = if (colors.isDark) colors.amber else Color(0xFFC2410C),
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChunkyButton(
                onClick = onRollDice,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.canRoll
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dice),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Tirar Dado", style = AppText.button)
            }
        }
    }
}

private const val EVIDENCE_NOTE = "Resuélvelo en tu libreta y sube tu procedimiento para poder contestar"

// ---------- Piezas de la pantalla ----------

/** .evidence-gate: pide la foto del procedimiento antes de mostrar las opciones de respuesta. */
@Composable
private fun EvidenceGate(
    showError: Boolean,
    onTakePhoto: () -> Unit,
    onPickPhoto: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = null,
            tint = colors.purple,
            modifier = Modifier.size(40.dp)
        )
        Text(
            text = "Esta dificultad requiere procedimiento. Resuelve el ejercicio de arriba en tu libreta " +
                "y sube una foto para poder ver las opciones de respuesta.",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = colors.mute,
            modifier = Modifier.widthIn(max = 300.dp)
        )
        ChunkyButton(
            onClick = onTakePhoto,
            depth = 4.dp,
            shape = RoundedCornerShape(14.dp),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 13.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_camera),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Tomar foto de mi procedimiento", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        SoftActionButton(
            text = "Elegir una foto de la galería",
            iconRes = R.drawable.ic_folder,
            enabled = true,
            onClick = onPickPhoto,
            containerColor = colors.blueSoft,
            borderColor = colors.blue.copy(alpha = 0.3f),
            contentColor = colors.blueText,
            fontSize = 12.5.sp
        )
        Text(
            text = if (showError) "No pudimos guardar esa foto. Inténtalo de nuevo o elige otra." else "",
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = if (colors.isDark) colors.red else colors.redDeep,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .height(16.dp)
        )
    }
}

/** Tres corazones; la mitad de vida se dibuja con un corazón lleno recortado a la mitad. */
@Composable
private fun Hearts(halfHearts: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until 3) {
            val fill = when {
                halfHearts >= 2 * (i + 1) -> 2
                halfHearts == 2 * i + 1 -> 1
                else -> 0
            }
            HeartIcon(fill)
        }
    }
}

/** [fill]: 2 = lleno, 1 = a la mitad, 0 = vacío. */
@Composable
private fun HeartIcon(fill: Int) {
    val tint = AppTheme.colors.red
    Box(modifier = Modifier.size(17.dp)) {
        Icon(
            painter = painterResource(if (fill == 2) R.drawable.ic_heart_full else R.drawable.ic_heart_empty),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
        if (fill == 1) {
            Icon(
                painter = painterResource(R.drawable.ic_heart_full),
                contentDescription = null,
                tint = tint,
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        clipRect(right = size.width / 2f) {
                            this@drawWithContent.drawContent()
                        }
                    }
            )
        }
    }
}

/** .timer-bar: barra ámbar que se vacía durante los 6 segundos de Rush. */
@Composable
private fun TimerBar(progress: Float) {
    val colors = AppTheme.colors
    val animated by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 100, easing = LinearEasing),
        label = "timer"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(7.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.line)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .background(colors.amber)
        )
    }
}

/** Botón suave con ícono (.howto-link y .reroll-btn). */
@Composable
private fun SoftActionButton(
    text: String,
    @DrawableRes iconRes: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    containerColor: Color,
    borderColor: Color,
    contentColor: Color,
    fontSize: TextUnit
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(10.dp)
    val tint = if (enabled) contentColor else colors.mute

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.6f)
            .clip(shape)
            .background(if (enabled) containerColor else colors.line.copy(alpha = 0.5f))
            .border(2.dp, if (enabled) borderColor else colors.line, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(7.dp))
        Text(text = text, fontSize = fontSize, fontWeight = FontWeight.ExtraBold, color = tint)
    }
}

// ---------- Ventanas emergentes ----------

@Composable
private fun DadoDialogs(
    dialog: DadoDialog?,
    state: DadoUiState,
    onDismiss: () -> Unit,
    onGameOverDismissed: () -> Unit
) {
    val colors = AppTheme.colors

    when (dialog) {
        null -> Unit

        is DadoDialog.RushWarning -> ModalDialog(onDismissRequest = onDismiss) {
            ModalIcon(R.drawable.ic_bolt, colors.amber)
            ModalTitle("Modo Rush")
            ModalText(
                "Elegiste Rush en dificultad ${dialog.level.displayName}. Para que te dé tiempo de responder, " +
                    "Rush genera los ejercicios con la dificultad de ${dialog.effective.displayName} " +
                    "en vez de ${dialog.level.displayName}."
            )
            ModalPrimaryButton(text = "Entendido", onClick = onDismiss)
        }

        is DadoDialog.GameOver -> ModalDialog(onDismissRequest = {}, dismissible = false) {
            ModalIcon(R.drawable.ic_broken_heart, colors.red)
            ModalTitle("¡Perdiste!")
            ModalText(
                if (dialog.hadProgressedBeyondEasy) {
                    "Te quedaste sin corazones. Se bloquea tu progreso y vuelves al nivel Fácil."
                } else {
                    "Te quedaste sin corazones en Fácil. ¡Vuelve a intentarlo!"
                }
            )
            ModalPrimaryButton(text = "Reintentar", onClick = onGameOverDismissed)
        }

        DadoDialog.HowTo -> ModalDialog(onDismissRequest = onDismiss) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScreenTitle(iconRes = R.drawable.ic_bulb, title = "Cómo se resuelve")
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Cerrar",
                    tint = colors.mute,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onDismiss)
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.steps.size > 1) {
                Text(
                    text = "Primero las multiplicaciones y divisiones, y después las sumas y restas, de izquierda a derecha.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.mute,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            state.steps.forEachIndexed { index, step ->
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                    if (state.steps.size > 1) {
                        Text(
                            text = "Paso ${index + 1}",
                            style = AppText.label,
                            color = colors.mute
                        )
                    }
                    Text(
                        text = "${NumberFormatter.format(step.a)} ${step.operation.symbol} " +
                            "${NumberFormatter.format(step.b)} = ${NumberFormatter.format(step.result)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.blueText
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            ModalPrimaryButton(text = "Cerrar", onClick = onDismiss)
        }
    }
}

@Composable
private fun ModalIcon(@DrawableRes iconRes: Int, tint: Color) {
    Icon(
        painter = painterResource(iconRes),
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(34.dp)
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ModalTitle(text: String) {
    Text(
        text = text,
        fontSize = 17.sp,
        fontWeight = FontWeight.ExtraBold,
        color = AppTheme.colors.ink,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun ModalText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 19.sp,
        color = AppTheme.colors.mute,
        textAlign = TextAlign.Center
    )
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun ModalPrimaryButton(text: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    ChunkyButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        containerColor = colors.blue,
        shadowColor = colors.blueDeep,
        depth = 4.dp,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}
