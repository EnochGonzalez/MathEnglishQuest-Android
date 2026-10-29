package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.rememberAppViewModelFactory

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onCreateAccount: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = rememberAppViewModelFactory())
) {
    val state by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onLoginSuccess()
    }

    AuthScreenContainer {
        AuthHero(title = "Math & English Quest", logoRes = R.drawable.ic_dice)

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
                viewModel.login()
            }
        )

        AuthErrorText(state.error)

        Spacer(modifier = Modifier.height(16.dp))

        AuthPrimaryButton(
            text = if (state.isLoading) "Entrando..." else "Entrar",
            onClick = {
                focusManager.clearFocus()
                viewModel.login()
            },
            enabled = !state.isLoading
        )

        AuthLink(text = "¿No tienes cuenta? Crear una", onClick = onCreateAccount)

        AuthDivider()

        AuthGuestButton(text = "Continuar sin correo", onClick = viewModel::continueAsGuest)

        AuthHint(
            "Ideal si no eres parte de ninguna clase. Podrás usar todos los módulos, " +
                "pero no tendrás portafolio en la nube ni código de clase."
        )
    }
}
