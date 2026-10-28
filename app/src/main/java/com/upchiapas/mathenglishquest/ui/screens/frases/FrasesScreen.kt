package com.upchiapas.mathenglishquest.ui.screens.frases

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.components.ChunkyButton
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
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
    val colors = AppTheme.colors

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
            .background(colors.background)
            .statusBarsPadding()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ScreenTitle(iconRes = R.drawable.ic_chat, title = "Frases en Inglés")

        Spacer(modifier = Modifier.height(15.dp))

        // .mode-selector / .mode-btn
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FrasesMode.entries.forEach { mode ->
                val isSelected = mode == uiState.activeMode
                ChunkyButton(
                    onClick = { onModeSelect(mode) },
                    modifier = Modifier.weight(1f),
                    containerColor = if (isSelected) colors.purple else colors.surface,
                    shadowColor = if (isSelected) colors.purpleDeep else Color.Transparent,
                    contentColor = if (isSelected) Color.White else colors.mute,
                    border = BorderStroke(2.dp, if (isSelected) colors.purple else colors.line),
                    depth = 3.dp,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val currentQ = uiState.currentQuestion
        if (currentQ != null) {
            // .equation-card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.surface)
                    .border(2.dp, colors.line, RoundedCornerShape(20.dp))
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                val op = if (currentQ.isAddition) "+" else "-"
                val questionDisplay = when (uiState.activeMode) {
                    FrasesMode.SOLO_LETRAS -> "${getWord(currentQ.num1)} $op ${getWord(currentQ.num2)}"
                    FrasesMode.NUMEROS_Y_LETRAS -> "${currentQ.num1} $op ${currentQ.num2}"
                    FrasesMode.TODO_EN_INGLES -> "${getWord(currentQ.num1)} $op ${getWord(currentQ.num2)}"
                }

                Text(
                    text = questionDisplay,
                    style = AppText.equation,
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Elige el resultado en inglés",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.mute
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.activeMode == FrasesMode.TODO_EN_INGLES) {
                // .voice-panel
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Intento ${uiState.attemptCount} de 3",
                        style = AppText.label,
                        color = colors.mute
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    ChunkyButton(
                        onClick = onVoiceButtonClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_mic),
                            contentDescription = "Micrófono",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = "Toca y responde en voz alta",
                            style = AppText.button
                        )
                    }

                    if (!uiState.voiceInlineMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = uiState.voiceInlineMessage,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.ink,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // .answers-grid
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    currentQ.options.forEach { optionText ->
                        OptionButton(
                            text = optionText,
                            isSelected = optionText == uiState.selectedOption,
                            isCorrect = optionText == uiState.correctOption,
                            isAnswered = uiState.isAnswered,
                            onClick = { onOptionClick(optionText) }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/** .answer-btn: normal, .correct (verde) o .wrong (rojo). */
@Composable
private fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    val showCorrect = isAnswered && isCorrect
    val showWrong = isAnswered && isSelected && !isCorrect

    val container = when {
        showCorrect -> colors.green
        showWrong -> colors.red
        else -> colors.surface
    }
    val deep = when {
        showCorrect -> colors.greenDeep
        showWrong -> colors.redDeep
        else -> colors.lineStrong
    }
    val borderColor = when {
        showCorrect -> colors.greenDeep
        showWrong -> colors.redDeep
        else -> colors.line
    }
    val textColor = if (showCorrect || showWrong) Color.White else colors.ink

    ChunkyButton(
        // enabled = true siempre: si estuviera desactivado el botón se vería al 40% y se perdería el verde/rojo
        onClick = { if (!isAnswered) onClick() },
        modifier = Modifier.fillMaxWidth(),
        containerColor = container,
        shadowColor = deep,
        contentColor = textColor,
        border = BorderStroke(2.dp, borderColor),
        depth = 4.dp,
        shape = RoundedCornerShape(15.dp),
        contentPadding = PaddingValues(13.dp)
    ) {
        Text(
            text = text,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/** .modal-card */
@Composable
private fun ModalCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppTheme.colors.surface)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
private fun VoiceResultDialog(
    answerText: String,
    onSpeakClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    val colors = AppTheme.colors

    Dialog(
        onDismissRequest = onContinueClick,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ModalCard {
            Icon(
                painter = painterResource(R.drawable.ic_chat),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "La respuesta era…",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = answerText,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                color = colors.ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // .voice-answer-listen
            ChunkyButton(
                onClick = onSpeakClick,
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.surface,
                shadowColor = Color.Transparent,
                contentColor = colors.purple,
                border = BorderStroke(2.dp, colors.purple),
                depth = 0.dp,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(12.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_mic),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Escuchar pronunciación",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            ChunkyButton(
                onClick = onContinueClick,
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.blue,
                shadowColor = colors.blueDeep,
                depth = 4.dp,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Continuar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun VoiceInstructionDialog(
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        ModalCard {
            Icon(
                painter = painterResource(R.drawable.ic_mic),
                contentDescription = null,
                tint = colors.purple,
                modifier = Modifier.size(34.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Antes de responder por voz",
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "El reconocimiento de voz puede fallar si hay mucho ruido alrededor, hablas muy rápido o el micrófono no te escucha bien. Si eso pasa, no te preocupes: solo vuelve a intentarlo.",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            ChunkyButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                containerColor = colors.blue,
                shadowColor = colors.blueDeep,
                depth = 4.dp,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Entendido",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
