package com.upchiapas.mathenglishquest.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upchiapas.mathenglishquest.R
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/** Botón "Escanear QR de la clase": abre el lector para llenar el código de clase sin escribirlo. */
@Composable
fun QrScanButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(2.dp, colors.line, shape)
            .clickable(onClick = onClick)
            .padding(13.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_qr),
            contentDescription = null,
            tint = colors.blueText,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = stringResource(R.string.qr_scan_button),
            fontSize = 13.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = colors.ink,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** Aviso debajo del botón: el código quedó listo, el QR no era de una clase, o no se pudo abrir el lector. */
@Composable
fun QrScanMessage(message: QrMessage?) {
    if (message == null) return
    val colors = AppTheme.colors
    val (text, color) = when (message) {
        QrMessage.SCANNED -> stringResource(R.string.qr_scan_ok) to colors.greenSoftText
        QrMessage.INVALID -> stringResource(R.string.qr_scan_invalid) to (if (colors.isDark) colors.red else colors.redDeep)
        QrMessage.FAILED -> stringResource(R.string.qr_scan_failed) to (if (colors.isDark) colors.red else colors.redDeep)
    }
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        lineHeight = 18.sp,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
