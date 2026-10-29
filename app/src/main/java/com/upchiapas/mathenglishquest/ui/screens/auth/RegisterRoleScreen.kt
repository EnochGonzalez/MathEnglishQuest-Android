package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.components.ChunkyCard
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun RegisterRoleScreen(
    onStudent: () -> Unit,
    onTeacher: () -> Unit,
    onBack: () -> Unit
) {
    val colors = AppTheme.colors

    AuthScreenContainer {
        AuthHero(title = "¿Cómo te registras?")

        RoleCard(
            iconRes = R.drawable.ic_person,
            iconBg = colors.greenSoft,
            iconTint = colors.greenSoftText,
            title = "Soy estudiante",
            subtitle = "Tengo un código de clase de mi maestro(a)",
            onClick = onStudent
        )
        RoleCard(
            iconRes = R.drawable.ic_cap,
            iconBg = colors.purpleSoft,
            iconTint = colors.purpleSoftText,
            title = "Soy maestro(a)",
            subtitle = "Voy a generar un código de clase",
            onClick = onTeacher
        )

        AuthLink(text = "← Volver a iniciar sesión", onClick = onBack)
    }
}

@Composable
private fun RoleCard(
    iconRes: Int,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors

    ChunkyCard(onClick = onClick) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = colors.ink)
            Text(text = subtitle, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = colors.mute)
        }
    }
}
