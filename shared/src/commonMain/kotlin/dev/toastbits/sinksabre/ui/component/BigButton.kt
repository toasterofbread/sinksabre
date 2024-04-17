package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.Dp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import dev.toastbits.composekit.utils.composable.AlignableCrossfade

private const val ICON_SIZE_DP: Float = 70f

@Composable
fun BigButton(
    onClick: () -> Unit,
    colour: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    full_content: Boolean = false,
    disabledContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val container_colour: Color by animateColorAsState(
        if (enabled) colour
        else colour.copy(alpha = 0.5f)
    )

    Card(
        onClick,
        modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = container_colour,
            contentColor = colour.getContrasted()
        ),
        enabled = enabled
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val icon_size: Dp = ICON_SIZE_DP.dp
            val icon_center_alignment: Alignment? =
                if (maxWidth < icon_size * 2 && maxHeight < icon_size * 2) Alignment.Center
                else if (maxWidth < icon_size * 2) Alignment.TopCenter
                else if (maxHeight < icon_size * 2) Alignment.CenterStart
                else null

            Crossfade(
                icon,
                Modifier.align(icon_center_alignment ?: Alignment.TopStart)
            ) {
                if (it != null) {
                    Icon(
                        it,
                        null,
                        Modifier.size(icon_size)
                    )
                }
            }

            AlignableCrossfade(
                if (icon_center_alignment == Alignment.Center) null
                else if (!enabled) disabledContent ?: content
                else content,
                contentAlignment = Alignment.Center
            ) { currentContent ->
                if (currentContent != null) {
                    val text_style: TextStyle =
                        LocalTextStyle.current.copy(
                            fontSize = 25.sp,
                            textAlign = TextAlign.Center
                        )

                    Box(
                        when (icon_center_alignment) {
                            Alignment.TopCenter -> Modifier.padding(top = icon_size)
                            Alignment.CenterStart -> Modifier.padding(start = icon_size)
                            else ->
                                if (icon_center_alignment == null && full_content) Modifier.padding(top = icon_size)
                                else Modifier
                        }
                    ) {
                        CompositionLocalProvider(LocalTextStyle provides text_style) {
                            currentContent?.invoke()
                        }
                    }
                }
            }
        }
    }
}

fun Color.getContrasted(keep_alpha: Boolean = false): Color {
    val base: Color =
        if (isDark()) Color.White
        else Color.Black
    return if (keep_alpha) base.copy(alpha = alpha) else base
}

fun Color.isDark(): Boolean =
    luminance() < 0.2
