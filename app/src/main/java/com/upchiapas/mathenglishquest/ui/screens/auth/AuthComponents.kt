package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.repository.AuthError
import com.upchiapas.mathenglishquest.ui.components.ChunkyButton
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** Texto de cada error (los mismos mensajes del prototipo web). */
fun AuthError.message(): String = when (this) {
    AuthError.CAMPOS_VACIOS -> "Completa todos los campos."
    AuthError.NOMBRE_INCOMPLETO -> "Escribe tu nombre completo: tu nombre y tus dos apellidos."
    AuthError.CORREO_INVALIDO -> "Escribe un correo válido, por ejemplo tucorreo@ejemplo.com."
    AuthError.PASSWORD_CORTA -> "La contraseña debe tener al menos 6 caracteres."
    AuthError.CORREO_EXISTENTE -> "Ya existe una cuenta con ese correo."
    AuthError.CODIGO_INEXISTENTE -> "Ese código de clase no existe. Revísalo con tu maestro(a)."
    AuthError.CREDENCIALES_INCORRECTAS -> "Correo o contraseña incorrectos."
    AuthError.DESCONOCIDO -> "Ocurrió un error. Inténtalo de nuevo."
}

/** Contenedor de todas las pantallas de login/registro (.page-content con padding-top 34px). */
@Composable
fun AuthScreenContainer(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 34.dp, bottom = 20.dp),
        content = content
    )
}

/** .auth-hero: logo opcional + título centrado. */
@Composable
fun AuthHero(title: String, @DrawableRes logoRes: Int? = null, logoTint: androidx.compose.ui.graphics.Color? = null) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (logoRes != null) {
            Icon(
                painter = painterResource(logoRes),
                contentDescription = null,
                tint = logoTint ?: colors.purple,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = title,
            fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
    }
}

/** .auth-label + .auth-input (+ .auth-hint opcional). */
@Composable
fun AuthField(
    label: String,
    @DrawableRes iconRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    isPassword: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    hint: String? = null
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.padding(top = 10.dp, bottom = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = colors.mute,
            modifier = Modifier.size(15.dp)
        )
        Text(text = label, style = AppText.label, color = colors.mute)
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.mute.copy(alpha = 0.7f)
            )
        },
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.ink),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }),
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

    if (hint != null) {
        Text(
            text = hint,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 15.sp,
            color = colors.mute,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** .auth-error (reserva su altura para que el diseño no salte al aparecer). */
@Composable
fun AuthErrorText(error: AuthError?) {
    val colors = AppTheme.colors
    Text(
        text = error?.message().orEmpty(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (colors.isDark) colors.red else colors.redDeep,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 16.dp)
            .padding(top = 8.dp)
    )
}

/** Botón morado grande (.dice-btn). */
@Composable
fun AuthPrimaryButton(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    ChunkyButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    ) {
        Text(text = text, style = AppText.button)
    }
}

/** .auth-link */
@Composable
fun AuthLink(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(top = 12.dp, bottom = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            textDecoration = TextDecoration.Underline,
            color = AppTheme.colors.blueText
        )
    }
}

/** .auth-divider: línea — "o" — línea */
@Composable
fun AuthDivider() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f).height(2.dp).background(colors.line))
        Text(
            text = "o",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.mute,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        Box(modifier = Modifier.weight(1f).height(2.dp).background(colors.line))
    }
}

/** .auth-guest-btn */
@Composable
fun AuthGuestButton(text: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.line, shape)
            .clickable(onClick = onClick)
            .padding(13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink
        )
    }
}

@Composable
fun AuthHint(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 15.sp,
        textAlign = TextAlign.Center,
        color = AppTheme.colors.mute,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
