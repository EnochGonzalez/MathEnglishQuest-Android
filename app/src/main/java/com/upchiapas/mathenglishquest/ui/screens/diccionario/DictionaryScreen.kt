package com.upchiapas.mathenglishquest.ui.screens.diccionario

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.domain.dictionary.WordEntry
import com.upchiapas.mathenglishquest.ui.components.ChunkyButton
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme
import com.upchiapas.mathenglishquest.utils.rememberSoundPlayer
import com.upchiapas.mathenglishquest.utils.rememberTextToSpeechHelper

@Composable
fun DictionaryScreen(
    viewModel: DictionaryViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    val soundPlayer = rememberSoundPlayer()
    val tts = rememberTextToSpeechHelper()

    DictionaryScreenContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::onSearch,
        onHistoryWordSelected = viewModel::onHistoryWordSelected,
        onHistoryWordRemoved = viewModel::onHistoryWordRemoved,
        onClearHistory = viewModel::onClearHistory,
        onListen = { entry ->
            // Primero el audio real que manda la API; si no trae o no se puede cargar, lo dice la voz del celular
            val url = entry.audioUrl
            if (url != null) {
                soundPlayer.playUrl(url, onError = { tts.speak(entry.word) })
            } else {
                tts.speak(entry.word)
            }
        }
    )
}

@Composable
fun DictionaryScreenContent(
    state: DictionaryUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onHistoryWordSelected: (String) -> Unit,
    onHistoryWordRemoved: (String) -> Unit,
    onClearHistory: () -> Unit,
    onListen: (WordEntry) -> Unit
) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        ScreenTitle(iconRes = R.drawable.ic_book, title = stringResource(R.string.dictionary_title))
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.dictionary_subtitle),
            style = AppText.pageSub,
            color = colors.mute
        )
        Spacer(modifier = Modifier.height(12.dp))

        // ---- Buscador ----
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = stringResource(R.string.dictionary_search_hint),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.mute.copy(alpha = 0.7f)
                    )
                },
                textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold, color = colors.ink),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }),
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
            ChunkyButton(
                onClick = {
                    focusManager.clearFocus()
                    onSearch()
                },
                enabled = state.status != DictionaryStatus.LOADING,
                depth = 4.dp,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 15.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = stringResource(R.string.dictionary_search_button),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { ResultSection(state = state, onRetry = onSearch, onListen = onListen) }

            item {
                if (state.history.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.dictionary_recent),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = colors.mute
                        )
                        Text(
                            text = stringResource(R.string.dictionary_clear_all),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textDecoration = TextDecoration.Underline,
                            color = colors.blueText,
                            modifier = Modifier
                                .clickable(onClick = onClearHistory)
                                .padding(vertical = 4.dp)
                        )
                    }
                } else if (state.status == DictionaryStatus.IDLE) {
                    Text(
                        text = stringResource(R.string.dictionary_first_time),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center,
                        color = colors.mute,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }

            items(state.history, key = { it }) { word ->
                HistoryRow(
                    word = word,
                    onClick = { onHistoryWordSelected(word) },
                    onRemove = { onHistoryWordRemoved(word) }
                )
            }
        }
    }
}

/** Lo que se ve según cómo va la búsqueda: cargando, la palabra, o el mensaje de lo que pasó. */
@Composable
private fun ResultSection(
    state: DictionaryUiState,
    onRetry: () -> Unit,
    onListen: (WordEntry) -> Unit
) {
    when (state.status) {
        DictionaryStatus.IDLE -> Unit

        DictionaryStatus.LOADING -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                color = AppTheme.colors.purple,
                strokeWidth = 3.dp,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.dictionary_loading),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colors.mute
            )
        }

        DictionaryStatus.SUCCESS -> state.entry?.let {
            WordCard(entry = it, fromCache = state.fromCache, onListen = { onListen(it) })
        }

        DictionaryStatus.NOT_FOUND ->
            MessageBox(text = stringResource(R.string.dictionary_not_found, state.searchedWord))

        DictionaryStatus.INVALID -> MessageBox(text = stringResource(R.string.dictionary_invalid))

        DictionaryStatus.NO_CONNECTION ->
            MessageBox(text = stringResource(R.string.dictionary_no_connection), onRetry = onRetry)

        DictionaryStatus.SERVER_ERROR -> MessageBox(
            text = state.serverErrorCode
                ?.let { stringResource(R.string.dictionary_server_error, it.toString()) }
                ?: stringResource(R.string.dictionary_server_error_generic),
            onRetry = onRetry
        )
    }
}

@Composable
private fun MessageBox(text: String, onRetry: (() -> Unit)? = null) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.line, shape)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center,
            color = colors.ink
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(10.dp))
            ChunkyButton(
                onClick = onRetry,
                containerColor = colors.blue,
                shadowColor = colors.blueDeep,
                depth = 4.dp,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = stringResource(R.string.dictionary_retry),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

/** La palabra con su pronunciación, el botón de escuchar y sus significados. */
@Composable
private fun WordCard(entry: WordEntry, fromCache: Boolean, onListen: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.line, shape)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.word,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.ink
                )
                entry.phonetic?.let {
                    Text(
                        text = it,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.mute
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            ChunkyButton(
                onClick = onListen,
                depth = 4.dp,
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 11.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_volume),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.dictionary_listen),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        if (fromCache) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.dictionary_from_cache),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.blueSoftText,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.blueSoft)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        entry.meanings.forEach { meaning ->
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = meaning.partOfSpeech,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.purpleSoftText,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.purpleSoft)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
            meaning.definitions.forEachIndexed { index, definition ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${index + 1}. ${definition.text}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 19.sp,
                    color = colors.ink
                )
                definition.example?.let {
                    Text(
                        text = stringResource(R.string.dictionary_example, it),
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        lineHeight = 18.sp,
                        color = colors.mute,
                        modifier = Modifier.padding(start = 14.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(word: String, onClick: () -> Unit, onRemove: () -> Unit) {
    val colors = AppTheme.colors
    ChunkyCard(
        onClick = onClick,
        contentPadding = PaddingValues(start = 14.dp, end = 6.dp, top = 6.dp, bottom = 6.dp)
    ) {
        Text(
            text = word,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_close),
                contentDescription = stringResource(R.string.dictionary_remove_word, word),
                tint = colors.mute,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
