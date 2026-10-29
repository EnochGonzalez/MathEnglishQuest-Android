package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** Tarjeta grande con la ecuación y una notita abajo (.equation-card). */
@Composable
fun EquationCard(
    equation: String,
    note: String,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.line, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = equation,
            style = AppText.equation,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        if (note.isNotEmpty()) {
            Text(
                text = note,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}
