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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.Crossfade
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import dev.toastbits.composekit.utils.composable.NullableValueAnimatedVisibility
import dev.toastbits.composekit.utils.common.thenIf
import androidx.compose.animation.core.tween
import androidx.compose.ui.composed
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon

private const val ICON_SIZE_DP: Float = 60f

@Composable
fun BigButton(
    onClick: () -> Unit,
    colour: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    icon_scale: Float = 1f,
    enabled: Boolean = true,
    clickable: Boolean = enabled,
    full_content: Boolean = false,
    bar_progress: Float? = null,
    disabledContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    val container_colour: Color by animateColorAsState(
        if (enabled) colour
        else colour.copy(alpha = 0.5f)
    )

    Card(
        onClick,
        modifier.thenIf(clickable) {
            hover()
        },
        shape = RoundedCornerShape(16.dp),
        colors =
            if (enabled && !clickable)
                CardDefaults.cardColors(
                    containerColor = container_colour,
                    disabledContainerColor = container_colour,
                    contentColor = colour.getContrasted(),
                    disabledContentColor = colour.getContrasted()
                )
            else
                CardDefaults.cardColors(
                    containerColor = container_colour,
                    contentColor = colour.getContrasted(),
                ),
        enabled = clickable
    ) {
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            val icon_size: Dp = ICON_SIZE_DP.dp * icon_scale
            val icon_center_alignment: Alignment? =
                if (content == null || (maxWidth < icon_size * 2 && maxHeight < icon_size * 2)) Alignment.Center
                else if (maxWidth < icon_size * 2) Alignment.TopCenter
                else if (maxHeight < icon_size * 2) Alignment.CenterStart
                else null

            Row(
                Modifier.align(icon_center_alignment ?: Alignment.TopStart),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Crossfade(icon) {
                    if (it != null) {
                        Icon(
                            it,
                            null,
                            Modifier.size(icon_size)
                        )
                    }
                }

                NullableValueAnimatedVisibility(bar_progress) {
                    if (it == null) {
                        return@NullableValueAnimatedVisibility
                    }

                    val progress: Float by animateFloatAsState(it)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            (progress * 100).toInt().toString() + "%",
                            Modifier.padding(bottom = 5.dp),
                            fontSize = 12.sp
                        )

                        LinearProgressIndicator(
                            progress = progress,
                            color = LocalContentColor.current,
                            modifier = Modifier.fillMaxWidth().weight(1f)
                        )
                    }
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

private fun Modifier.hover(): Modifier = composed {
    val interaction_source: MutableInteractionSource = remember { MutableInteractionSource() }
    val hovered: Boolean by interaction_source.collectIsHoveredAsState()

    val scale: Float by animateFloatAsState(
        if (hovered) 1.025f else 1f,
        animationSpec = tween(100)
    )

    return@composed this
        .hoverable(interaction_source)
        .scale(scale)
        .pointerHoverIcon(PointerIcon.Hand)
}
