package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/**
 * Opción de respuesta (.answer-btn del prototipo): normal, correcta (verde) o equivocada (roja).
 * [isSelected] = la que tocó el estudiante; [isCorrect] = la que es correcta; ambas se pintan solo cuando [isAnswered].
 */
@Composable
fun AnswerButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isAnswered: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
        // enabled = true siempre: si estuviera desactivado se vería al 40% y se perdería el verde/rojo
        onClick = { if (!isAnswered) onClick() },
        modifier = modifier.fillMaxWidth(),
        containerColor = container,
        shadowColor = deep,
        contentColor = textColor,
        border = BorderStroke(2.dp, borderColor),
        depth = 4.dp,
        shape = RoundedCornerShape(15.dp),
        contentPadding = PaddingValues(13.dp)
    ) {
        Text(text = text, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}
