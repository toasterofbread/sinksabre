package dev.toastbits.sinksabre

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import dev.toastbits.sinksabre.ui.AppTheme
import dev.toastbits.sinksabre.ui.layout.*
import dev.toastbits.sinksabre.ui.component.BigButton
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.sinksabre.sync.SyncMethod
import androidx.compose.material3.LocalContentColor

@Composable
fun SinkSabre(context: AppContext) {
    LaunchedEffect(Unit) {
        if (context.settings.SYNC_ON_START.get()) {
            // TODO
        }
    }

    AppTheme {
        Content(context)
    }
}

@Composable
private fun Content(context: AppContext) {
    val spacing: Dp = 20.dp
    val arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(spacing)

    var current_menu: Menu by remember { mutableStateOf(Menu.DEFAULT) }
    val sync_method: SyncMethod? by SyncMethod.observe(context)

    Row(
        Modifier.fillMaxSize().background(Color.Black).padding(spacing),
        horizontalArrangement = arrangement
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(Modifier.fillMaxWidth(0.5f), verticalArrangement = arrangement) {
                current_menu.Content(context, Modifier.fillMaxSize().weight(1f))

                Row(
                    Modifier.heightIn(max = 100.dp)
                ) {
                    BigButton(
                        {
                            current_menu =
                                if (current_menu == Menu.SETTINGS) Menu.DEFAULT
                                else Menu.SETTINGS
                        },
                        Color(0xFF2A2B2A),
                        Modifier
                            .fillMaxWidth(0.5f)
                            .padding(end = spacing / 2),
                        icon =
                            if (current_menu == Menu.SETTINGS) Icons.Default.Close
                            else Icons.Default.Settings
                    ) {
                        Text("Settings")
                    }

                    BigButton(
                        {
                            current_menu =
                                if (current_menu == Menu.INSPECT) Menu.DEFAULT
                                else Menu.INSPECT
                        },
                        Color(0xFF2A2B2A),
                        Modifier
                            .fillMaxWidth()
                            .weight(1f, false)
                            .padding(start = spacing / 2),
                        icon =
                            if (current_menu == Menu.INSPECT) Icons.Default.Close
                            else Icons.Default.Visibility
                    ) {
                        Text("View songs")
                    }
                }
            }

            Column(
                Modifier.fillMaxSize().weight(1f),
                verticalArrangement = arrangement
            ) {
                BigButton(
                    {
                        context.launchBeatSaber()
                    },
                    Color(0xFF64B6AC),
                    Modifier.fillMaxWidth().fillMaxHeight(0.5f),
                    icon = Icons.Default.PlayArrow
                ) {
                    Text("Launch")
                }

                BigButton(
                    { 
                        val method: SyncMethod = sync_method ?: return@BigButton
                        
                    },
                    Color(0xFFE5446D),
                    Modifier.fillMaxSize().weight(1f),
                    icon = Icons.Default.Refresh,
                    enabled = sync_method?.isConfigured() == true,
                    disabledContent = {
                        Text("Sync method not configured")
                    }
                ) {
                    Text("Sync")
                }
            }
        }
    }
}

private enum class Menu {
    LANDING, SETTINGS, INSPECT;

    @Composable
    fun Content(context: AppContext, modifier: Modifier = Modifier) {
        when (this) {
            LANDING -> LandingMenu(context, modifier)
            SETTINGS -> SettingsMenu(context, modifier)
            INSPECT -> InspectMenu(context, modifier)
        }
    }

    companion object {
        val DEFAULT: Menu = LANDING
    }
}
