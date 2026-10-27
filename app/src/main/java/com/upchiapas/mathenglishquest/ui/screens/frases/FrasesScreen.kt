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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.upchiapas.mathenglishquest.ui.theme.AppBackground
import com.upchiapas.mathenglishquest.ui.theme.BlueAccent
import com.upchiapas.mathenglishquest.ui.theme.BorderLight
import com.upchiapas.mathenglishquest.ui.theme.CardBackground
import com.upchiapas.mathenglishquest.ui.theme.PinkAccent
import com.upchiapas.mathenglishquest.ui.theme.PinkLightBg
import com.upchiapas.mathenglishquest.ui.theme.PurpleAccent
import com.upchiapas.mathenglishquest.ui.theme.PurpleLightBg
import com.upchiapas.mathenglishquest.ui.theme.TextPrimary
import com.upchiapas.mathenglishquest.ui.theme.TextSecondary
import com.upchiapas.mathenglishquest.utils.getVoiceRecognizerIntent
import com.upchiapas.mathenglishquest.utils.rememberTextToSpeechHelper
import com.upchiapas.mathenglishquest.utils.rememberVoiceRecognizerLauncher
import kotlin.random.Random

enum class FrasesMode(val title: String) {
    SOLO_LETRAS("Solo letras"),
    NUMEROS_Y_LETRAS("Números y letras"),
    TODO_EN_INGLES("Todo en inglés")
}

data class FrasesQuestion(
    val num1: Int,
    val num2: Int,
    val isAddition: Boolean,
    val options: List<String>
) {
    val result: Int = if (isAddition) num1 + num2 else num1 - num2
}

private val ENGLISH_WORDS = mapOf(
    0 to "Zero", 1 to "One", 2 to "Two", 3 to "Three", 4 to "Four",
    5 to "Five", 6 to "Six", 7 to "Seven", 8 to "Eight", 9 to "Nine",
    10 to "Ten", 11 to "Eleven", 12 to "Twelve", 13 to "Thirteen", 14 to "Fourteen",
    15 to "Fifteen", 16 to "Sixteen", 17 to "Seventeen", 18 to "Eighteen", 19 to "Nineteen", 20 to "Twenty"
)

private fun getWord(num: Int): String = ENGLISH_WORDS[num] ?: num.toString()

private fun generateQuestion(): FrasesQuestion {
    val isAdd = Random.nextBoolean()
    var n1 = Random.nextInt(1, 11)
    var n2 = Random.nextInt(1, 11)
    if (!isAdd && n1 < n2) {
        val temp = n1
        n1 = n2
        n2 = temp
    }
    val correctRes = if (isAdd) n1 + n2 else n1 - n2
    val correctWord = getWord(correctRes)

    val wrongOptions = mutableSetOf<String>()
    while (wrongOptions.size < 2) {
        val fakeRes = Random.nextInt(0, 21)
        if (fakeRes != correctRes) {
            wrongOptions.add(getWord(fakeRes))
        }
    }
    val allOptions = (wrongOptions.toList() + correctWord).shuffled()

    return FrasesQuestion(n1, n2, isAdd, allOptions)
}

@Composable
fun FrasesScreen() {
    val context = LocalContext.current
    val ttsHelper = rememberTextToSpeechHelper()

    var activeMode by remember { mutableStateOf(FrasesMode.SOLO_LETRAS) }
    var currentQuestion by remember { mutableStateOf(generateQuestion()) }
    var attemptCount by remember { mutableIntStateOf(1) }
    var showVoiceModal by remember { mutableStateOf(false) }

    // Launcher for voice recognition
    val voiceLauncher = rememberVoiceRecognizerLauncher { spokenText ->
        val correctWord = getWord(currentQuestion.result)
        val isCorrect = spokenText.contains(correctWord, ignoreCase = true) ||
                spokenText.contains(currentQuestion.result.toString(), ignoreCase = true)

        if (isCorrect) {
            Toast.makeText(context, "¡Excelente! Respuesta correcta: $correctWord", Toast.LENGTH_SHORT).show()
            ttsHelper.speak("Correct! $correctWord")
            currentQuestion = generateQuestion()
            attemptCount = 1
        } else {
            if (attemptCount < 3) {
                attemptCount++
                Toast.makeText(context, "Dijiste: \"$spokenText\". Inténtalo de nuevo.", Toast.LENGTH_SHORT).show()
                ttsHelper.speak("Try again")
            } else {
                Toast.makeText(context, "Intento agotado. La respuesta era $correctWord", Toast.LENGTH_SHORT).show()
                currentQuestion = generateQuestion()
                attemptCount = 1
            }
        }
    }

    // Show modal dialog when switching to "Todo en inglés"
    LaunchedEffect(activeMode) {
        if (activeMode == FrasesMode.TODO_EN_INGLES) {
            showVoiceModal = true
        }
    }

    if (showVoiceModal) {
        VoiceInstructionDialog(
            onDismiss = { showVoiceModal = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
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
                val isSelected = mode == activeMode
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
                        .clickable {
                            activeMode = mode
                            attemptCount = 1
                            currentQuestion = generateQuestion()
                        }
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
                val questionDisplay = when (activeMode) {
                    FrasesMode.SOLO_LETRAS -> {
                        val op = if (currentQuestion.isAddition) "+" else "-"
                        "${getWord(currentQuestion.num1)} $op ${getWord(currentQuestion.num2)}"
                    }

                    FrasesMode.NUMEROS_Y_LETRAS -> {
                        val op = if (currentQuestion.isAddition) "+" else "-"
                        "${currentQuestion.num1} $op ${currentQuestion.num2}"
                    }

                    FrasesMode.TODO_EN_INGLES -> {
                        val op = if (currentQuestion.isAddition) "+" else "-"
                        "${getWord(currentQuestion.num1)} $op ${getWord(currentQuestion.num2)}"
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
        if (activeMode == FrasesMode.TODO_EN_INGLES) {
            // Voice Mode
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Intento $attemptCount de 3",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        try {
                            voiceLauncher.launch(getVoiceRecognizerIntent())
                        } catch (_: Exception) {
                            Toast.makeText(
                                context,
                                "Reconocimiento de voz no disponible en este dispositivo",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
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

                Spacer(modifier = Modifier.height(24.dp))

                // Also provide option buttons as alternative/fallback
                Text(
                    text = "O selecciona una opción:",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                currentQuestion.options.forEach { optionText ->
                    OptionButton(
                        text = optionText,
                        onClick = {
                            val correctWord = getWord(currentQuestion.result)
                            if (optionText == correctWord) {
                                Toast.makeText(context, "¡Correcto!", Toast.LENGTH_SHORT).show()
                                ttsHelper.speak("Correct! $correctWord")
                                currentQuestion = generateQuestion()
                                attemptCount = 1
                            } else {
                                Toast.makeText(context, "Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                                ttsHelper.speak("Try again")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        } else {
            // Options Mode (Solo letras & Números y letras)
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                currentQuestion.options.forEach { optionText ->
                    OptionButton(
                        text = optionText,
                        onClick = {
                            val correctWord = getWord(currentQuestion.result)
                            if (optionText == correctWord) {
                                Toast.makeText(context, "¡Correcto!", Toast.LENGTH_SHORT).show()
                                ttsHelper.speak("Correct! $correctWord")
                                currentQuestion = generateQuestion()
                                attemptCount = 1
                            } else {
                                Toast.makeText(context, "Incorrecto. Inténtalo de nuevo", Toast.LENGTH_SHORT).show()
                                ttsHelper.speak("Try again")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OptionButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
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
                color = TextPrimary
            )
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
