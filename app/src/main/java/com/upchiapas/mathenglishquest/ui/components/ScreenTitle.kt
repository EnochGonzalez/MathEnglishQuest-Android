package com.upchiapas.mathenglishquest.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** Título de módulo: ícono de 18dp + texto (".top-bar .title" del prototipo). */
@Composable
fun ScreenTitle(
    @DrawableRes iconRes: Int,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = AppTheme.colors.ink,
            modifier = Modifier.size(18.dp)
        )
        Text(text = title, style = AppText.topBarTitle, color = AppTheme.colors.ink)
    }
}
