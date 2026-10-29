package com.upchiapas.mathenglishquest.ui.screens.portfolio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
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

@Composable
fun PortfolioScreen(
    viewModel: PortfolioViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    PortfolioScreenContent(state = state, fileFor = viewModel::fileFor)
}

@Composable
fun PortfolioScreenContent(
    state: PortfolioUiState,
    fileFor: (EvidenceEntry) -> File
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
        ScreenTitle(iconRes = R.drawable.ic_folder, title = "Portafolio")
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Todas tus evidencias, ordenadas de la más reciente a la más antigua.",
            style = AppText.pageSub,
            color = colors.mute
        )
        Spacer(modifier = Modifier.height(14.dp))

        if (!state.isLoading && state.groups.isEmpty()) {
            Text(
                text = "Todavía no hay evidencia aquí.\nCuando subas la foto de un procedimiento en el Dado, aparecerá en este lugar.",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                color = colors.mute,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp, start = 24.dp, end = 24.dp)
            )
        } else {
            LazyVerticalGrid(
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
private fun EvidenceCard(
    entry: EvidenceEntry,
    file: File,
    onClick: () -> Unit
) {
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
            Text(
                text = entry.categories.joinToString(" · "),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AppTheme.colors.ink,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
            )
        }
    }
}
