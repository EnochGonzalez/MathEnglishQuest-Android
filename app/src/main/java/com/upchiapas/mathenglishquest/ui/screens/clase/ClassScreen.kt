package com.upchiapas.mathenglishquest.ui.screens.clase

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
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.domain.clase.ClassRoster
import com.upchiapas.mathenglishquest.domain.clase.RosterOrder
import com.upchiapas.mathenglishquest.domain.clase.StudentSummary
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.components.ScreenTitle
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun ClassScreen(
    onOpenStudent: (Int) -> Unit,
    viewModel: ClassViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    ClassScreenContent(
        state = state,
        onOpenStudent = onOpenStudent,
        onQueryChange = viewModel::onQueryChange,
        onOrderSelected = viewModel::onOrderSelected
    )
}

@Composable
fun ClassScreenContent(
    state: ClassUiState,
    onOpenStudent: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
    onOrderSelected: (RosterOrder) -> Unit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
    ) {
        ScreenTitle(iconRes = R.drawable.ic_cap, title = "Mi Clase")
        Spacer(modifier = Modifier.height(15.dp))

        if (!state.isLoading && state.classCode == null) {
            Message("Mi Clase es solo para maestros. Entra con una cuenta de maestro(a) para ver a tus estudiantes.")
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // ---- Código de clase (.home-hero del prototipo) ----
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(colors.inverseBg)
                        .padding(20.dp)
                ) {
                    Text(
                        text = "TU CÓDIGO DE CLASE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF93C5FD)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // Se puede mantener presionado para copiarlo
                    SelectionContainer {
                        Text(
                            text = state.classCode ?: "------",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp,
                            color = colors.onInverse
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Compártelo con tus estudiantes para que se vinculen a tu clase.",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 19.sp,
                        color = colors.onInverse.copy(alpha = 0.85f)
                    )
                }
            }

            // ---- Encabezado de la lista, búsqueda y orden ----
            item {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = if (state.totalStudents > 0) "Estudiantes vinculados (${state.totalStudents})" else "Estudiantes vinculados",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.mute
                    )
                    if (state.totalStudents > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SearchField(value = state.query, onValueChange = onQueryChange)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            RosterOrder.entries.forEach { order ->
                                OrderChip(
                                    label = order.label,
                                    isSelected = order == state.order,
                                    onClick = { onOrderSelected(order) }
                                )
                            }
                        }
                    }
                }
            }

            // ---- Estudiantes ----
            if (!state.isLoading && state.totalStudents == 0) {
                item {
                    Message("Aún no tienes estudiantes vinculados. Comparte tu código de clase con ellos.")
                }
            } else if (!state.isLoading && state.students.isEmpty()) {
                item { Message("Ningún estudiante coincide con tu búsqueda.") }
            } else {
                items(state.students, key = { it.id }) { student ->
                    StudentRow(student = student, onClick = { onOpenStudent(student.id) })
                }
            }
        }
    }
}

@Composable
private fun Message(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 20.sp,
        textAlign = TextAlign.Center,
        color = AppTheme.colors.mute,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, start = 12.dp, end = 12.dp)
    )
}

@Composable
private fun StudentRow(student: StudentSummary, onClick: () -> Unit) {
    val colors = AppTheme.colors
    ChunkyCard(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.blueSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_person),
                contentDescription = null,
                tint = colors.blueSoftText,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = student.name,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.ink
            )
            Text(
                text = ClassRoster.activityText(student),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute
            )
        }
    }
}

@Composable
private fun SearchField(value: String, onValueChange: (String) -> Unit) {
    val colors = AppTheme.colors
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = "Buscar por nombre o correo",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute.copy(alpha = 0.7f)
            )
        },
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.ink),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
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
}

@Composable
private fun OrderChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
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
