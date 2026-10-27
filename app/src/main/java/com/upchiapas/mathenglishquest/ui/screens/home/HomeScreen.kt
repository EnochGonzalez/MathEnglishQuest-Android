package com.upchiapas.mathenglishquest.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.upchiapas.mathenglishquest.ui.components.UserTopHeader
import com.upchiapas.mathenglishquest.ui.navigation.Screen
import com.upchiapas.mathenglishquest.ui.theme.AppBackground
import com.upchiapas.mathenglishquest.ui.theme.BorderLight
import com.upchiapas.mathenglishquest.ui.theme.CardBackground
import com.upchiapas.mathenglishquest.ui.theme.GreenAccent
import com.upchiapas.mathenglishquest.ui.theme.GreenLightBg
import com.upchiapas.mathenglishquest.ui.theme.NavyPrimary
import com.upchiapas.mathenglishquest.ui.theme.PinkAccent
import com.upchiapas.mathenglishquest.ui.theme.PinkLightBg
import com.upchiapas.mathenglishquest.ui.theme.PurpleDark
import com.upchiapas.mathenglishquest.ui.theme.PurpleLightBg
import com.upchiapas.mathenglishquest.ui.theme.TextPrimary
import com.upchiapas.mathenglishquest.ui.theme.TextSecondary

@Composable
fun HomeScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        UserTopHeader()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Hero Card - Tu Misión de Hoy
            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = NavyPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "TU MISIÓN DE HOY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF93C5FD),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elige un módulo",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Practica matemáticas y vocabulario en inglés. Cada racha de 3 desbloquea más dificultad.",
                        fontSize = 14.sp,
                        color = Color(0xFFE2E8F0),
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Module 1: Dado
            ModuleCard(
                title = "Módulo del Dado",
                description = "Retos de matemáticas por dificultad",
                icon = Icons.Default.Casino,
                iconBgColor = PurpleLightBg,
                iconColor = PurpleDark,
                onClick = { navController.navigate(Screen.Dado.route) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Module 2: Números
            ModuleCard(
                title = "Números en Inglés",
                description = "Del 1 al 20 y más, con pronunciación",
                icon = Icons.Default.Abc,
                iconBgColor = GreenLightBg,
                iconColor = GreenAccent,
                onClick = { navController.navigate(Screen.Numeros.route) }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Module 3: Frases
            ModuleCard(
                title = "Frases en Inglés",
                description = "Operaciones y vocabulario en inglés",
                icon = Icons.Outlined.ChatBubbleOutline,
                iconBgColor = PinkLightBg,
                iconColor = PinkAccent,
                onClick = { navController.navigate(Screen.Frases.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModuleCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
