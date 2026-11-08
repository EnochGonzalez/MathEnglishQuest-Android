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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.UserTopHeader
import com.upchiapas.mathenglishquest.ui.navigation.Screen
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToDado = { navController.navigate(Screen.Dado.route) },
        onNavigateToNumeros = { navController.navigate(Screen.Numeros.route) },
        onNavigateToFrases = { navController.navigate(Screen.Frases.route) },
        onNavigateToDiccionario = { navController.navigate(Screen.Diccionario.route) },
        onNavigateToPortfolio = { navController.navigate(Screen.Portfolio.route) },
        onNavigateToClase = { navController.navigate(Screen.Clase.route) },
        onLogout = {
            viewModel.logout()
            navController.navigate(Screen.Login.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    onNavigateToDado: () -> Unit,
    onNavigateToNumeros: () -> Unit,
    onNavigateToFrases: () -> Unit,
    onNavigateToDiccionario: () -> Unit,
    onNavigateToPortfolio: () -> Unit,
    onNavigateToClase: () -> Unit,
    onLogout: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        UserTopHeader(userName = uiState.userName, role = uiState.role, onLogoutClick = onLogout)

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
                        text = stringResource(R.string.home_mision_caption),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = uiState.missionTitle.asString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.onInverse
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = uiState.missionSubtitle.asString(),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp,
                        color = colors.onInverse.copy(alpha = 0.85f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            ModuleCard(
                title = stringResource(R.string.module_dado_title),
                description = stringResource(R.string.module_dado_desc),
                iconRes = R.drawable.ic_dice,
                iconBgColor = colors.purpleSoft,
                iconColor = colors.purpleSoftText,
                onClick = onNavigateToDado
            )

            Spacer(modifier = Modifier.height(7.dp))

            ModuleCard(
                title = stringResource(R.string.module_numeros_title),
                description = stringResource(R.string.module_numeros_desc),
                iconRes = R.drawable.ic_letters,
                iconBgColor = colors.greenSoft,
                iconColor = colors.greenSoftText,
                onClick = onNavigateToNumeros
            )

            Spacer(modifier = Modifier.height(7.dp))

            ModuleCard(
                title = stringResource(R.string.module_frases_title),
                description = stringResource(R.string.module_frases_desc),
                iconRes = R.drawable.ic_chat,
                iconBgColor = colors.redSoft,
                iconColor = colors.redSoftText,
                onClick = onNavigateToFrases
            )

            Spacer(modifier = Modifier.height(7.dp))

            ModuleCard(
                title = stringResource(R.string.module_diccionario_title),
                description = stringResource(R.string.module_diccionario_desc),
                iconRes = R.drawable.ic_book,
                iconBgColor = colors.blueSoft,
                iconColor = colors.blueSoftText,
                onClick = onNavigateToDiccionario
            )

            // Solo estudiantes con cuenta
            if (uiState.role == UserRole.ESTUDIANTE) {
                Spacer(modifier = Modifier.height(7.dp))

                ModuleCard(
                    title = stringResource(R.string.module_portfolio_title),
                    description = stringResource(R.string.module_portfolio_desc),
                    iconRes = R.drawable.ic_folder,
                    iconBgColor = colors.yellowSoft,
                    iconColor = colors.yellowSoftText,
                    onClick = onNavigateToPortfolio
                )
            }

            // Solo maestros
            if (uiState.role == UserRole.MAESTRO) {
                Spacer(modifier = Modifier.height(7.dp))

                ModuleCard(
                    title = stringResource(R.string.module_clase_title),
                    description = stringResource(R.string.module_clase_desc),
                    iconRes = R.drawable.ic_cap,
                    iconBgColor = colors.blueSoft,
                    iconColor = colors.blueSoftText,
                    onClick = onNavigateToClase
                )
            }

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
