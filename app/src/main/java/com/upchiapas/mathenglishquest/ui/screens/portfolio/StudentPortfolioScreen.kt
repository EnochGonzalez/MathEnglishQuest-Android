package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.ui.rememberStudentPortfolioViewModelFactory
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** El maestro ve el portafolio de un estudiante de su clase (solo lectura). */
@Composable
fun StudentPortfolioScreen(
    studentId: Int,
    onBack: () -> Unit,
    viewModel: StudentPortfolioViewModel = viewModel(
        key = "student_portfolio_$studentId",
        factory = rememberStudentPortfolioViewModelFactory(studentId)
    )
) {
    val state by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors

    if (state.accessDenied) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Text(
                text = "← Volver al panel",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textDecoration = TextDecoration.Underline,
                color = colors.blueText,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(bottom = 24.dp)
            )
            Text(
                text = "Este estudiante no pertenece a tu clase.",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = colors.mute,
                modifier = Modifier.fillMaxWidth()
            )
        }
        return
    }

    val name = state.studentName
    PortfolioScreenContent(
        state = state.portfolio,
        fileFor = viewModel::fileFor,
        onDifficultyFilterSelected = viewModel::onDifficultyFilterSelected,
        title = name ?: "Portafolio",
        subtitle = if (name != null) "Evidencias de $name, de la más reciente a la más antigua." else "",
        emptyText = "Este estudiante todavía no ha subido evidencias.",
        onBack = onBack
    )
}
