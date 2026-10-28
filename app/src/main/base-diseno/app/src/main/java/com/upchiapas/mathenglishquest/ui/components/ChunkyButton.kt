package com.upchiapas.mathenglishquest.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.upchiapas.mathenglishquest.ui.theme.AppTheme

/**
 * Botón con sombra dura de "botón físico" (como .dice-btn / .answer-btn / .module-btn del prototipo).
 * Al presionar baja [depth] y la sombra desaparece.
 */
@Composable
fun ChunkyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = AppTheme.colors.purple,
    shadowColor: Color = AppTheme.colors.purpleDeep,
    contentColor: Color = Color.White,
    border: BorderStroke? = null,
    enabled: Boolean = true,
    depth: Dp = 6.dp,
    shape: Shape = RoundedCornerShape(18.dp),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    content: @Composable RowScope.() -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val offset by animateDpAsState(
        targetValue = if (pressed && enabled) depth else 0.dp,
        animationSpec = tween(durationMillis = 60),
        label = "chunkyPress"
    )

    Box(
        propagateMinConstraints = true,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.4f)
            .drawBehind {
                if (enabled) {
                    val d = depth.toPx()
                    val outline = shape.createOutline(
                        Size(size.width, size.height - d), layoutDirection, this
                    )
                    translate(top = d) {
                        drawOutline(outline, color = shadowColor)
                    }
                }
            }
            .padding(bottom = depth)
    ) {
        Row(
            modifier = Modifier
                .offset(y = offset)
                .clip(shape)
                .background(containerColor, shape)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .clickable(
                    interactionSource = interaction,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(contentPadding),
            horizontalArrangement = horizontalArrangement,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                content()
            }
        }
    }
}

/** Tarjeta clicable con borde de 2dp y sombra de 3dp (.module-btn, .num-card). */
@Composable
fun ChunkyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(16.dp),
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    content: @Composable RowScope.() -> Unit
) {
    ChunkyButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        containerColor = AppTheme.colors.surface,
        shadowColor = AppTheme.colors.lineStrong,
        contentColor = AppTheme.colors.ink,
        border = BorderStroke(2.dp, AppTheme.colors.line),
        enabled = enabled,
        depth = 3.dp,
        shape = shape,
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        content = content
    )
}
