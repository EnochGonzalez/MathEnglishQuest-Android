package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.Difficulty
import com.upchiapas.mathenglishquest.domain.portfolio.EvidenceEntry
import com.upchiapas.mathenglishquest.domain.portfolio.PortfolioGrouping
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.EvidenceImage
import com.upchiapas.mathenglishquest.ui.components.ModalDialog
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import java.io.File

/** Portafolio del estudiante con la sesión abierta. */
@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    PortfolioScreenContent(
        state = state,
        fileFor = viewModel::fileFor,
        onDifficultyFilterSelected = viewModel::onDifficultyFilterSelected,
        title = "Portafolio",
        subtitle = "Todas tus evidencias, ordenadas de la más reciente a la más antigua.",
        emptyText = "Todavía no hay evidencia aquí.\nCuando subas la foto de un procedimiento en el Dado, aparecerá en este lugar."
    )
}

/** Se usa tanto para el portafolio propio como para el que ve el maestro (con [onBack] y otro texto). */
@Composable
fun PortfolioScreenContent(
    state: PortfolioUiState,
    fileFor: (EvidenceEntry) -> File,
    onDifficultyFilterSelected: (String?) -> Unit,
    title: String,
    subtitle: String,
    emptyText: String,
    onBack: (() -> Unit)? = null
) {
    val colors = AppTheme.colors
    var selected by remember { mutableStateOf<EvidenceEntry?>(null) }

    // Vista grande de una foto (lightbox)
    selected?.let { entry ->
        ModalDialog(onDismissRequest = { selected = null }) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = "Cerrar",
                    tint = colors.mute,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selected = null }
                        .padding(8.dp)
                )
            }
            EvidenceImage(
                file = fileFor(entry),
                maxSidePx = 1280,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .clip(RoundedCornerShape(14.dp))
            )
            Spacer(modifier = Modifier.height(10.dp))
            if (entry.exercise.isNotBlank()) {
                Text(
                    text = entry.exercise,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.ink,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = "${entry.difficulty} · ${entry.categories.joinToString(" · ")}",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.mute,
                textAlign = TextAlign.Center
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        if (onBack != null) {
            Text(
                text = "← Volver al panel",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                textDecoration = TextDecoration.Underline,
                color = colors.blueText,
                modifier = Modifier
                    .clickable(onClick = onBack)
                    .padding(bottom = 12.dp)
            )
        }
        ScreenTitle(iconRes = R.drawable.ic_folder, title = title)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = subtitle, style = AppText.pageSub, color = colors.mute)
        Spacer(modifier = Modifier.height(10.dp))

        // Filtros por dificultad: solo tienen sentido cuando ya hay evidencias
        if (state.totalCount > 0) {
            DifficultyFilterRow(selected = state.difficultyFilter, onSelect = onDifficultyFilterSelected)
            Spacer(modifier = Modifier.height(10.dp))
        }

        when {
            state.isLoading -> Unit

            state.totalCount == 0 -> CenteredMessage(emptyText)

            state.groups.isEmpty() -> CenteredMessage("No hay evidencias con esta dificultad.")

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                state.groups.forEach { group ->
                    item(key = "day_${group.dayStartMillis}", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = PortfolioGrouping.dayLabel(group.dayStartMillis),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.mute,
                            modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
                        )
                    }
                    items(group.entries, key = { it.id }) { entry ->
                        EvidenceCard(
                            entry = entry,
                            file = fileFor(entry),
                            onClick = { selected = entry }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CenteredMessage(text: String) {
    Text(
        text = text,
        fontSize = 13.5.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 22.sp,
        textAlign = TextAlign.Center,
        color = AppTheme.colors.mute,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 50.dp, start = 24.dp, end = 24.dp)
    )
}

/** Chips "Todas · Fácil · Medio · Difícil · Muy Difícil" (con scroll horizontal si no caben). */
@Composable
private fun DifficultyFilterRow(
    selected: String?,
    onSelect: (String?) -> Unit
) {
    val options: List<Pair<String, String?>> =
        listOf("Todas" to null) + Difficulty.entries.map { it.displayName to it.displayName }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            FilterChip(label = label, isSelected = selected == value, onClick = { onSelect(value) })
        }
    }
}

@Composable
private fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Text(
        text = label,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        color = if (isSelected) colors.onInverse else colors.mute,
        modifier = Modifier
            .clip(shape)
            .background(if (isSelected) colors.inverseBg else colors.surface)
            .border(2.dp, if (isSelected) colors.inverseBg else colors.line, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
private fun EvidenceCard(
    entry: EvidenceEntry,
    file: File,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    ChunkyCard(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            EvidenceImage(
                file = file,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)) {
                if (entry.exercise.isNotBlank()) {
                    Text(
                        text = entry.exercise,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${entry.difficulty} · ${entry.categories.joinToString(" · ")}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.mute,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
