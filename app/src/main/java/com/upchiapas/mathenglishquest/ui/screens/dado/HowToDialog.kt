package com.upchiapas.mathenglishquest.ui.screens.dado

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.domain.howto.CustomSolveResult
import com.upchiapas.mathenglishquest.domain.howto.HowToBuilder
import com.upchiapas.mathenglishquest.domain.howto.HowToSolution
import com.upchiapas.mathenglishquest.ui.components.HowToBlocksView
import com.upchiapas.mathenglishquest.ui.components.ModalDialog
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** "Cómo se resuelve": el procedimiento de cada paso del ejercicio, y una calculadora para probar otras operaciones. */
@Composable
fun HowToDialog(
    solutions: List<HowToSolution>,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors

    ModalDialog(onDismissRequest = onDismiss) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ScreenTitle(iconRes = R.drawable.ic_bulb, title = "Cómo se resuelve")
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = "Cerrar",
                tint = colors.mute,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onDismiss)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // El contenido se desliza si no cabe (una división larga tiene varios pasos)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 480.dp)
                .verticalScroll(rememberScrollState())
        ) {
            solutions.forEach { solution ->
                SolutionView(solution)
                Spacer(modifier = Modifier.height(20.dp))
            }
            CustomSolver()
        }

        Spacer(modifier = Modifier.height(10.dp))
        ModalPrimaryButton(text = "Cerrar", onClick = onDismiss)
    }
}

@Composable
private fun SolutionView(solution: HowToSolution) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = solution.title,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = AppTheme.colors.ink
        )
        Spacer(modifier = Modifier.height(8.dp))
        HowToBlocksView(solution.blocks)
    }
}

/** .custom-solver: escribir "100/30" y ver su procedimiento. */
@Composable
private fun CustomSolver() {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var solution by remember { mutableStateOf<HowToSolution?>(null) }

    fun solve() {
        focusManager.clearFocus()
        when (val result = HowToBuilder.solveCustom(input)) {
            is CustomSolveResult.Failure -> {
                error = result.message
                solution = null
            }
            is CustomSolveResult.Success -> {
                error = ""
                solution = result.solution
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Raya punteada de separación (border-top: 2px dashed)
                drawLine(
                    color = colors.line,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx()))
                )
            }
            .padding(top = 16.dp)
    ) {
        Text(
            text = "¿Quieres resolver otra operación? Escríbela aquí:",
            style = AppText.label,
            color = colors.mute
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "ej. 100/30",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.mute.copy(alpha = 0.7f)
                    )
                },
                textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.ink),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { solve() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.blue,
                    unfocusedBorderColor = colors.line,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                    cursorColor = colors.blue,
                    focusedTextColor = colors.ink,
                    unfocusedTextColor = colors.ink
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.inverseBg)
                    .clickable { solve() }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Resolver",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.onInverse
                )
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (colors.isDark) colors.red else colors.redDeep,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        solution?.let {
            Spacer(modifier = Modifier.height(14.dp))
            SolutionView(it)
        }
    }
}
