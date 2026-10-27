package com.upchiapas.mathenglishquest.ui.screens.dado

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.ui.theme.AppBackground
import com.upchiapas.mathenglishquest.ui.theme.BorderLight
import com.upchiapas.mathenglishquest.ui.theme.CardBackground
import com.upchiapas.mathenglishquest.ui.theme.PurpleAccent
import com.upchiapas.mathenglishquest.ui.theme.PurpleDark
import com.upchiapas.mathenglishquest.ui.theme.PurpleLightBg
import com.upchiapas.mathenglishquest.ui.theme.TextPrimary
import kotlin.random.Random

enum class DadoDifficulty(val title: String) {
    FACIL("Fácil"),
    MEDIO("Medio"),
    DIFICIEL("Difícil")
}

@Composable
fun DadoScreen() {
    val context = LocalContext.current
    var selectedDifficulty by remember { mutableStateOf(DadoDifficulty.FACIL) }
    var dice1 by remember { mutableIntStateOf(1) }
    var dice2 by remember { mutableIntStateOf(1) }
    var rotationAngle by remember { mutableStateOf(0f) }
    var streakCount by remember { mutableIntStateOf(0) }

    val rotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 400),
        label = "diceRoll"
    )

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
                    .background(PurpleLightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = PurpleDark,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Módulo del Dado",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Difficulty Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DadoDifficulty.entries.forEach { diff ->
                val isSelected = diff == selectedDifficulty
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) PurpleDark else CardBackground)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) PurpleDark else BorderLight,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { selectedDifficulty = diff }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = diff.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dice Challenge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Racha actual: 🔥 $streakCount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PurpleAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Dice Display
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DiceVisual(value = dice1, rotation = rotation)
                    if (selectedDifficulty != DadoDifficulty.FACIL) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        DiceVisual(value = dice2, rotation = rotation)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val result = if (selectedDifficulty == DadoDifficulty.FACIL) dice1 else dice1 + dice2

                Text(
                    text = "Resultado: $result",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        rotationAngle += 360f
                        dice1 = Random.nextInt(1, 7)
                        dice2 = Random.nextInt(1, 7)
                        streakCount++
                        Toast.makeText(context, "¡Dado lanzado!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleDark)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lanzar Dado",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DiceVisual(
    value: Int,
    rotation: Float
) {
    Box(
        modifier = Modifier
            .size(70.dp)
            .rotate(rotation)
            .clip(RoundedCornerShape(16.dp))
            .background(PurpleLightBg)
            .border(2.dp, PurpleAccent, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.toString(),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = PurpleDark
        )
    }
}
