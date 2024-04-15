package dev.toastbits.sinksabre

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import dev.toastbits.sinksabre.ui.component.BigButton

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SinkSabre() {
    val spacing: Dp = 20.dp
    val arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(spacing)

    var current_menu: Menu? by remember { mutableStateOf(null) }
    if (current_menu != null) {

    }

    Row(
        Modifier.fillMaxSize().background(Color.Black).padding(spacing),
        horizontalArrangement = arrangement
    ) {
        Column(Modifier.fillMaxWidth(0.5f), verticalArrangement = arrangement) {
            BigButton(
                { TODO() },
                Color(0xFFE5446D),
                Modifier.fillMaxSize().weight(1f),
                icon = Icons.Default.Refresh
            ) {
                Text("Refresh")
            }

            Row(Modifier.wrapContentHeight()) {
                BigButton(
                    { current_menu = Menu.SETTINGS },
                    Color(0xFF2A2B2A),
                    Modifier.fillMaxWidth(0.5f).padding(end = spacing / 2).aspectRatio(1f),
                    icon = Icons.Default.Settings
                )
                BigButton(
                    { current_menu = Menu.INFO },
                    Color(0xFF2A2B2A),
                    Modifier.fillMaxWidth().weight(1f, false).padding(start = spacing / 2).aspectRatio(1f),
                    icon = Icons.Default.Info
                )
            }
        }

        BigButton(
            { TODO() },
            Color(0xFF64B6AC),
            Modifier.fillMaxSize().weight(1f),
            icon = Icons.Default.PlayArrow
        ) {
            Text("Launch game")
        }
    }
}

private enum class Menu {
    SETTINGS, INFO
}
