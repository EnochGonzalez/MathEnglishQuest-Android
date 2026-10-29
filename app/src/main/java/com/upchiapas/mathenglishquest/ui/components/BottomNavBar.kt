package com.upchiapas.mathenglishquest.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.navigation.Screen
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

sealed class BottomNavItem(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int
) {
    object Home : BottomNavItem(Screen.Home.route, "Inicio", R.drawable.ic_home)
    object Dado : BottomNavItem(Screen.Dado.route, "Dado", R.drawable.ic_dice)
    object Numeros : BottomNavItem(Screen.Numeros.route, "Números", R.drawable.ic_letters)
    object Frases : BottomNavItem(Screen.Frases.route, "Frases", R.drawable.ic_chat)
    object Portafolio : BottomNavItem(Screen.Portfolio.route, "Portafolio", R.drawable.ic_folder)

    companion object {
        /** Los 4 módulos que ven todos (invitados incluidos). */
        val base: List<BottomNavItem> get() = listOf(Home, Dado, Numeros, Frases)
    }
}

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    items: List<BottomNavItem> = BottomNavItem.base
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .navigationBarsPadding()
    ) {
        // border-top: 2px solid var(--line)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(colors.line)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                val tint = if (isSelected) colors.blueText else colors.mute

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!isSelected) onNavigate(item.route)
                        }
                        .padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 12.dp)
                ) {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.title,
                        tint = tint,
                        modifier = Modifier.size(21.dp)
                    )
                    Text(text = item.title, style = AppText.navLabel, color = tint)
                }
            }
        }
    }
}
