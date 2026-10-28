package com.upchiapas.mathenglishquest.ui.screens.frases

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.ui.theme.AppBackground
import com.upchiapas.mathenglishquest.ui.theme.BlueAccent
import com.upchiapas.mathenglishquest.ui.theme.BorderLight
import com.upchiapas.mathenglishquest.ui.theme.CardBackground
import com.upchiapas.mathenglishquest.ui.theme.PinkAccent
import com.upchiapas.mathenglishquest.ui.theme.PinkLightBg
import com.upchiapas.mathenglishquest.ui.theme.PurpleAccent
import com.upchiapas.mathenglishquest.ui.theme.PurpleDark
import com.upchiapas.mathenglishquest.ui.theme.PurpleLightBg
import com.upchiapas.mathenglishquest.ui.theme.TextPrimary
import com.upchiapas.mathenglishquest.ui.theme.TextSecondary
import com.upchiapas.mathenglishquest.utils.getVoiceRecognizerIntent
import com.upchiapas.mathenglishquest.utils.rememberTextToSpeechHelper
import com.upchiapas.mathenglishquest.utils.rememberVoiceRecognizerLauncher

@Composable
fun FrasesScreen(
    viewModel: FrasesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val ttsHelper = rememberTextToSpeechHelper()

    val voiceLauncher = rememberVoiceRecognizerLauncher { spokenText ->
        viewModel.handleVoiceResult(spokenText)
    }

    LaunchedEffect(uiState.speechToSpeak) {
        uiState.speechToSpeak?.let { speech ->
            ttsHelper.speak(speech)
            viewModel.clearSpeech()
        }
    }

    FrasesScreenContent(
        uiState = uiState,
        getWord = { num -> viewModel.getWord(num) },
        onModeSelect = { viewModel.selectMode(it) },
        onDismissVoiceModal = { viewModel.dismissVoiceModal() },
        onDismissResultModal = { viewModel.dismissResultModal() },
        onSpeakModalAnswer = { viewModel.speakAnswer() },
        onOptionClick = { option -> viewModel.checkOptionAnswer(option) },
        onVoiceButtonClick = {
            try {
                voiceLauncher.launch(getVoiceRecognizerIntent())
            } catch (_: Exception) {
                Toast.makeText(
                    context,
                    "Reconocimiento de voz no disponible en este dispositivo",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    )
}

@Composable
fun FrasesScreenContent(
    uiState: FrasesUiState,
    getWord: (Int) -> String,
    onModeSelect: (FrasesMode) -> Unit,
    onDismissVoiceModal: () -> Unit,
    onDismissResultModal: () -> Unit,
    onSpeakModalAnswer: () -> Unit,
    onOptionClick: (String) -> Unit,
    onVoiceButtonClick: () -> Unit
) {
    if (uiState.showVoiceModal) {
        VoiceInstructionDialog(onDismiss = onDismissVoiceModal)
    }

    if (uiState.showResultModal) {
        VoiceResultDialog(
            answerText = uiState.resultModalAnswer,
            onSpeakClick = onSpeakModalAnswer,
            onContinueClick = onDismissResultModal
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PinkLightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubbleOutline,
                    contentDescription = null,
                    tint = PinkAccent,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Frases en Inglés",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selector Tabs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FrasesMode.entries.forEach { mode ->
                val isSelected = mode == uiState.activeMode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) PurpleAccent else CardBackground)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PurpleAccent else BorderLight,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onModeSelect(mode) }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextPrimary,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Question Card
        val currentQ = uiState.currentQuestion
        if (currentQ != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val questionDisplay = when (uiState.activeMode) {
                        FrasesMode.SOLO_LETRAS -> {
                            val op = if (currentQ.isAddition) "+" else "-"
                            "${getWord(currentQ.num1)} $op ${getWord(currentQ.num2)}"
                        }

                        FrasesMode.NUMEROS_Y_LETRAS -> {
                            val op = if (currentQ.isAddition) "+" else "-"
                            "${currentQ.num1} $op ${currentQ.num2}"
                        }

                        FrasesMode.TODO_EN_INGLES -> {
                            val op = if (currentQ.isAddition) "+" else "-"
                            "${getWord(currentQ.num1)} $op ${getWord(currentQ.num2)}"
                        }
                    }

                    Text(
                        text = questionDisplay,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Elige el resultado en inglés",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Content per Mode
            if (uiState.activeMode == FrasesMode.TODO_EN_INGLES) {
                // Mode 3: Voice Only (No manual options)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Intento ${uiState.attemptCount} de 3",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onVoiceButtonClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleAccent)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Micrófono",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Toca y responde en voz alta",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    if (!uiState.voiceInlineMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiState.voiceInlineMessage,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Modes 1 & 2: Option Buttons with Green / Red feedback
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    currentQ.options.forEach { optionText ->
                        val isSelected = optionText == uiState.selectedOption
                        val isCorrect = optionText == uiState.correctOption

                        OptionButton(
                            text = optionText,
                            isSelected = isSelected,
                            isCorrect = isCorrect,
                            isAnswered = uiState.isAnswered,
                            onClick = { onOptionClick(optionText) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val greenColor = Color(0xFF22C55E)
    val redColor = Color(0xFFEF4444)

    val containerColor = when {
        isAnswered && isCorrect -> greenColor
        isAnswered && isSelected -> redColor
        else -> CardBackground
    }

    val textColor = if (isAnswered && (isCorrect || isSelected)) Color.White else TextPrimary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAnswered) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
private fun VoiceResultDialog(
    answerText: String,
    onSpeakClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    Dialog(onDismissRequest = onContinueClick) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(PurpleLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "La respuesta era...",
                    fontSize = 15.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = answerText,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, PurpleAccent)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Escuchar pronunciación",
                            tint = PurpleAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Escuchar pronunciación",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleDark,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text(
                        text = "Continuar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun VoiceInstructionDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(PurpleLightBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Antes de responder por voz",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "El reconocimiento de voz puede fallar si hay mucho ruido alrededor, hablas muy rápido o el micrófono no te escucha bien. Si eso pasa, no te preocupes: solo vuelve a intentarlo.",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text(
                        text = "Entendido",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
