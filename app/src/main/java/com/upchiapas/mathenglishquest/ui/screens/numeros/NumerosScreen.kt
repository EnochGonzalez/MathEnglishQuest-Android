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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            ttsHelper.speak(item.englishText)
        }
    )
}

@Composable
fun NumerosScreenContent(
    uiState: NumerosUiState,
    onCategorySelect: (NumberCategory) -> Unit,
    onNumberCardClick: (NumberItem) -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp)
    ) {
        ScreenTitle(iconRes = R.drawable.ic_letters, title = "Números en Inglés")

        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Toca una tarjeta para escuchar la pronunciación.",
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
                        text = category.title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        color = if (isSelected) colors.onInverse else colors.mute
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // .card-grid (3 columnas, gap 9px; 3dp de cada tarjeta ya son sombra)
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
