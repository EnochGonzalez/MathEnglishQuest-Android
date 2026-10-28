package com.upchiapas.mathenglishquest.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.UserTopHeader
import com.upchiapas.mathenglishquest.ui.navigation.Screen
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToDado = { navController.navigate(Screen.Dado.route) },
        onNavigateToNumeros = { navController.navigate(Screen.Numeros.route) },
        onNavigateToFrases = { navController.navigate(Screen.Frases.route) }
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToDado: () -> Unit,
    onNavigateToNumeros: () -> Unit,
    onNavigateToFrases: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        UserTopHeader(userName = uiState.userName, role = uiState.role)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // .home-hero
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(colors.inverseBg)
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "TU MISIÓN DE HOY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.missionTitle,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onInverse
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.missionSubtitle,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp,
                        color = colors.onInverse.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ModuleCard(
                title = "Módulo del Dado",
                description = "Retos de matemáticas por dificultad",
                iconRes = R.drawable.ic_dice,
                iconBgColor = colors.purpleSoft,
                iconColor = colors.purpleSoftText,
                onClick = onNavigateToDado
            )

            Spacer(modifier = Modifier.height(7.dp))

            ModuleCard(
                title = "Números en Inglés",
                description = "Del 1 al 20 y más, con pronunciación",
                iconRes = R.drawable.ic_letters,
                iconBgColor = colors.greenSoft,
                iconColor = colors.greenSoftText,
                onClick = onNavigateToNumeros
            )

            Spacer(modifier = Modifier.height(7.dp))

            ModuleCard(
                title = "Frases en Inglés",
                description = "Operaciones y vocabulario en inglés",
                iconRes = R.drawable.ic_chat,
                iconBgColor = colors.redSoft,
                iconColor = colors.redSoftText,
                onClick = onNavigateToFrases
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ModuleCard(
    title: String,
    description: String,
    @DrawableRes iconRes: Int,
    iconBgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    ChunkyCard(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink
            )
            Text(
                text = description,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute
            )
        }
    }
}
