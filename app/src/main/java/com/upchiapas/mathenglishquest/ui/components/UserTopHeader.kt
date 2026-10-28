package com.upchiapas.mathenglishquest.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.data.model.UserRole
import com.upchiapas.mathenglishquest.ui.theme.AppText
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

@Composable
fun UserTopHeader(
    userName: String = "Invitado",
    role: UserRole = UserRole.INVITADO,
    onLogoutClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hola, $userName",
            style = AppText.topBarTitle,
            color = colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(start = 8.dp)
        ) {
            val badgeBg = if (role == UserRole.MAESTRO) colors.purpleSoft else colors.blueSoft
            val badgeText = if (role == UserRole.MAESTRO) colors.purpleSoftText else colors.blueSoftText

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(badgeBg)
                    .padding(horizontal = 9.dp, vertical = 3.dp)
            ) {
                Text(
                    text = role.label.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.3.sp,
                    color = badgeText
                )
            }

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.line.copy(alpha = 0.6f))
                    .clickable {
                        if (onLogoutClick != null) {
                            onLogoutClick()
                        } else {
                            Toast
                                .makeText(context, "Sesión de invitado", Toast.LENGTH_SHORT)
                                .show()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = "Cerrar sesión",
                    tint = colors.mute,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
