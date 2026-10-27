package com.upchiapas.mathenglishquest.ui.screens.numeros

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.ui.theme.AppBackground
import com.upchiapas.mathenglishquest.ui.theme.BorderLight
import com.upchiapas.mathenglishquest.ui.theme.CardBackground
import com.upchiapas.mathenglishquest.ui.theme.GreenAccent
import com.upchiapas.mathenglishquest.ui.theme.GreenLightBg
import com.upchiapas.mathenglishquest.ui.theme.NavyPrimary
import com.upchiapas.mathenglishquest.ui.theme.TextPrimary
import com.upchiapas.mathenglishquest.ui.theme.TextSecondary
import com.upchiapas.mathenglishquest.utils.rememberTextToSpeechHelper

data class NumberItem(
    val number: Int,
    val englishText: String
)

enum class NumberCategory(val title: String) {
    BASICOS("Básicos"),
    DECENAS("Decenas"),
    CENTENAS("Centenas"),
    MILES("Miles")
}

@Composable
fun NumerosScreen() {
    val ttsHelper = rememberTextToSpeechHelper()
    var selectedCategory by remember { mutableStateOf(NumberCategory.BASICOS) }

    val numbersList = remember(selectedCategory) {
        when (selectedCategory) {
            NumberCategory.BASICOS -> listOf(
                NumberItem(1, "One"),
                NumberItem(2, "Two"),
                NumberItem(3, "Three"),
                NumberItem(4, "Four"),
                NumberItem(5, "Five"),
                NumberItem(6, "Six"),
                NumberItem(7, "Seven"),
                NumberItem(8, "Eight"),
                NumberItem(9, "Nine"),
                NumberItem(10, "Ten"),
                NumberItem(11, "Eleven"),
                NumberItem(12, "Twelve"),
                NumberItem(13, "Thirteen"),
                NumberItem(14, "Fourteen"),
                NumberItem(15, "Fifteen"),
                NumberItem(16, "Sixteen"),
                NumberItem(17, "Seventeen"),
                NumberItem(18, "Eighteen"),
                NumberItem(19, "Nineteen")
            )

            NumberCategory.DECENAS -> listOf(
                NumberItem(20, "Twenty"),
                NumberItem(30, "Thirty"),
                NumberItem(40, "Forty"),
                NumberItem(50, "Fifty"),
                NumberItem(60, "Sixty"),
                NumberItem(70, "Seventy"),
                NumberItem(80, "Eighty"),
                NumberItem(90, "Ninety"),
                NumberItem(100, "One Hundred")
            )

            NumberCategory.CENTENAS -> listOf(
                NumberItem(100, "One Hundred"),
                NumberItem(200, "Two Hundred"),
                NumberItem(300, "Three Hundred"),
                NumberItem(400, "Four Hundred"),
                NumberItem(500, "Five Hundred"),
                NumberItem(600, "Six Hundred"),
                NumberItem(700, "Seven Hundred"),
                NumberItem(800, "Eight Hundred"),
                NumberItem(900, "Nine Hundred")
            )

            NumberCategory.MILES -> listOf(
                NumberItem(1000, "One Thousand"),
                NumberItem(2000, "Two Thousand"),
                NumberItem(3000, "Three Thousand"),
                NumberItem(4000, "Four Thousand"),
                NumberItem(5000, "Five Thousand"),
                NumberItem(6000, "Six Thousand"),
                NumberItem(7000, "Seven Thousand"),
                NumberItem(8000, "Eight Thousand"),
                NumberItem(9000, "Nine Thousand")
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        // Header Row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GreenLightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Abc,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Números en Inglés",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Toca una tarjeta para escuchar la pronunciación.",
            fontSize = 13.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category Tabs Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberCategory.entries.forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) NavyPrimary else CardBackground)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NavyPrimary else BorderLight,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategory = category }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid of Numbers
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(numbersList, key = { "${it.number}_${it.englishText}" }) { item ->
                NumberCard(
                    item = item,
                    onClick = {
                        ttsHelper.speak(item.englishText)
                    }
                )
            }
        }
    }
}

@Composable
private fun NumberCard(
    item: NumberItem,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        label = "cardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
                isPressed = false
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = item.number.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.englishText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
    }
}
