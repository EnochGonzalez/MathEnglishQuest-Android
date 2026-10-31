package com.upchiapas.mathenglishquest.ui.screens.numeros

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import com.upchiapas.mathenglishquest.utils.rememberTextToSpeechHelper

@Composable
fun NumerosScreen(
    viewModel: NumerosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val ttsHelper = rememberTextToSpeechHelper()

    NumerosScreenContent(
        uiState = uiState,
        onCategorySelect = { viewModel.selectCategory(it) },
        onNumberCardClick = { item ->
            val shouldSpeak = viewModel.onNumberCardClick(item)
            if (shouldSpeak) {
                ttsHelper.speak(item.englishText)
            }
        },
        onBackFromDecade = { viewModel.clearSelectedDecade() }
    )
}

@Composable
fun NumerosScreenContent(
    uiState: NumerosUiState,
    onCategorySelect: (NumberCategory) -> Unit,
    onNumberCardClick: (NumberItem) -> Unit,
    onBackFromDecade: () -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        ScreenTitle(iconRes = R.drawable.ic_letters, title = stringResource(R.string.numeros_title))

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.numeros_subtitle),
            style = AppText.pageSub,
            color = colors.mute
        )

        Spacer(modifier = Modifier.height(10.dp))

        // .tabs-row / .tab-chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            NumberCategory.entries.forEach { category ->
                val isSelected = category == uiState.selectedCategory
                val shape = RoundedCornerShape(20.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (isSelected) colors.inverseBg else colors.surface)
                        .border(
                            width = 2.dp,
                            color = if (isSelected) colors.inverseBg else colors.line,
                            shape = shape
                        )
                        .clickable { onCategorySelect(category) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(category.titleRes),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        color = if (isSelected) colors.onInverse else colors.mute
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Si hay una decena seleccionada, mostrar barra de volver
        if (uiState.selectedDecade != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .border(2.dp, colors.line, RoundedCornerShape(12.dp))
                        .clickable { onBackFromDecade() }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = colors.purple,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.back_to_decenas),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.purple
                    )
                }

                Text(
                    text = stringResource(R.string.decade_title, uiState.selectedDecade),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.ink
                )
            }
        }

        // .card-grid (3 columnas)
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(uiState.numbersList, key = { "${it.number}_${it.englishText}" }) { item ->
                NumberCard(
                    item = item,
                    onClick = { onNumberCardClick(item) }
                )
            }
        }
    }
}

@Composable
private fun NumberCard(
    item: NumberItem,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    ChunkyCard(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = item.number.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink
            )
            Text(
                text = item.englishText,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = colors.mute,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
