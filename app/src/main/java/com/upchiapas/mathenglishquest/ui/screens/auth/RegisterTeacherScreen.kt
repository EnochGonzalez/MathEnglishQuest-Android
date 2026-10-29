package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory

@Composable
fun RegisterTeacherScreen(
    onSuccess: (classCode: String) -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterTeacherViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.generatedCode) {
        state.generatedCode?.let(onSuccess)
    }

    AuthScreenContainer {
        AuthHero(title = "Registro de maestro(a)")

        AuthField(
            label = "Nombre completo",
            iconRes = R.drawable.ic_person,
            value = state.name,
            onValueChange = viewModel::onNameChange,
            placeholder = "Tu nombre y dos apellidos",
            capitalization = KeyboardCapitalization.Words,
            hint = "Escribe tu nombre y tus dos apellidos (mínimo 3 palabras)."
        )
        AuthField(
            label = "Correo",
            iconRes = R.drawable.ic_mail,
            value = state.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "tucorreo@ejemplo.com",
            keyboardType = KeyboardType.Email
        )
        AuthField(
            label = "Contraseña",
            iconRes = R.drawable.ic_lock,
            value = state.password,
            onValueChange = viewModel::onPasswordChange,
            placeholder = "••••••••",
            isPassword = true,
            imeAction = ImeAction.Done,
            onImeAction = {
                focusManager.clearFocus()
                viewModel.register()
            }
        )

        AuthErrorText(state.error)

        Spacer(modifier = Modifier.height(16.dp))

        AuthPrimaryButton(
            text = if (state.isLoading) "Registrando..." else "Registrarme",
            onClick = {
                focusManager.clearFocus()
                viewModel.register()
            },
            enabled = !state.isLoading
        )

        AuthLink(text = "← Volver", onClick = onBack)
    }
}
