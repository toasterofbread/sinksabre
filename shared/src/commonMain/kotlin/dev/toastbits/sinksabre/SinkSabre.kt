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
import androidx.compose.runtime.mutableStateListOf
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
import dev.toastbits.sinksabre.ui.component.SyncButton
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.composekit.utils.composable.LinkifyText
import dev.toatsbits.sinksabre.model.LocalSong
import androidx.compose.material3.LocalContentColor
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

private class State {
    val spacing: Dp = 20.dp
    val arrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(spacing)

    var current_menu: Menu by mutableStateOf(Menu.DEFAULT)

    var session_added_songs: MutableList<LocalSong> = mutableStateListOf()
}

@Composable
fun SinkSabre(context: AppContext) {
    val state: State = remember { State() }

    AppTheme {
        state.Content(context)
    }
}

@Composable
private fun State.Content(context: AppContext) {
    var scroll_warning_dismissed: Boolean by context.settings.SCROLL_WARNING_DISMISSED.observe()

    if (!scroll_warning_dismissed) {
        AlertDialog(
            { },
            confirmButton = {
                Button({ scroll_warning_dismissed = true }) {
                    Text("OK")
                }
            },
            title = {
                Text("Warning")
            },
            text = {
                LinkifyText(
                    "Scrolling using the controller stick may cause the app to crash\n\nMore info: https://issuetracker.google.com/issues/314269723",
                    highlight_colour = Color(0xFF0ca8eb)
                )
            }
        )
    }

    Row(
        Modifier.fillMaxSize().background(Color.Black).padding(spacing),
        horizontalArrangement = arrangement
    ) {
        CompositionLocalProvider(LocalContentColor provides Color.White) {
            Column(Modifier.fillMaxWidth(0.5f), verticalArrangement = arrangement) {
                current_menu.Content(context, this@Content, Modifier.fillMaxSize().weight(1f))
                MenuButtonsRow(Modifier.heightIn(max = 100.dp))
            }

            Column(
                Modifier.fillMaxSize().weight(1f),
                verticalArrangement = arrangement
            ) {
                var sync_button_showing_content: Boolean by remember { mutableStateOf(false) }
                var sync_in_progress: Boolean by remember { mutableStateOf(false) }

                val launch_button_height: Float by animateFloatAsState(
                    if (sync_button_showing_content) 0.2f
                    else 0.5f
                )

                BigButton(
                    {
                        context.launchBeatSaber()
                    },
                    Color(0xFF64B6AC),
                    Modifier.fillMaxWidth().fillMaxHeight(launch_button_height),
                    icon = Icons.Default.PlayArrow,
                    enabled = context.canLaunchBeatSaber() && !sync_in_progress,
                    disabledContent = {
                        if (!context.canLaunchBeatSaber()) {
                            Text("Cannot launch")
                        }
                        else if (sync_in_progress) {
                            Text("Sync in progress")
                        }
                    }
                ) {
                    Text("Launch")
                }

                SyncButton(
                    context,
                    Modifier.fillMaxSize().weight(1f),
                    onShowingContentChanged = { sync_button_showing_content = it },
                    onSyncingChanged = { sync_in_progress = it },
                    onSongsAdded = { added ->
                        for (song in added) {
                            if (session_added_songs.none { it.hash == song.hash }) {
                                session_added_songs.add(song)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun State.MenuButtonsRow(modifier: Modifier = Modifier) {
    Row(modifier) {
        val settings_fill_proportion: Float by animateFloatAsState(if (current_menu == Menu.SETTINGS) 1f else 0.5f)

        AnimatedVisibility(!current_menu.is_inspect) {
            BigButton(
                {
                    current_menu =
                        if (current_menu == Menu.SETTINGS) Menu.DEFAULT
                        else Menu.SETTINGS
                },
                Color(0xFF2A2B2A),
                Modifier
                    .fillMaxWidth(settings_fill_proportion)
                    .padding(end = spacing / 2),
                icon =
                    if (current_menu == Menu.SETTINGS) Icons.Default.Close
                    else Icons.Default.Settings
            ) {
                if (current_menu == Menu.SETTINGS) {
                    Text("Close")
                }
                else {
                    Text("Settings")
                }
            }
        }

        AnimatedVisibility(current_menu.is_inspect) {
            BigButton(
                {
                    current_menu =
                        if (current_menu == Menu.INSPECT_LOCAL) Menu.INSPECT_REMOTE
                        else Menu.INSPECT_LOCAL
                },
                Color(0xFF2A2B2A),
                Modifier
                    .aspectRatio(1f)
                    .padding(end = spacing / 2),
                icon =
                    if (current_menu == Menu.INSPECT_LOCAL) Icons.Default.Cloud
                    else Icons.Default.Storage,
                icon_scale = 0.6f
            )
        }

        AnimatedVisibility(current_menu != Menu.SETTINGS) {
            BigButton(
                {
                    if (current_menu.is_inspect) {
                        current_menu = Menu.DEFAULT
                    }
                    else {
                        current_menu = Menu.INSPECT_LOCAL
                    }
                },
                Color(0xFF2A2B2A),
                Modifier
                    .fillMaxWidth()
                    .weight(1f, false)
                    .padding(start = spacing / 2),
                icon =
                    if (current_menu.is_inspect) Icons.Default.Close
                    else Icons.Default.Visibility
            ) {
                if (current_menu.is_inspect) {
                    Text("Close")
                }
                else {
                    Text("View maps")
                }
            }
        }
    }
}

private enum class Menu {
    LANDING, SETTINGS, INSPECT_LOCAL, INSPECT_REMOTE;

    val is_inspect: Boolean get() = this == INSPECT_LOCAL || this == INSPECT_REMOTE

    @Composable
    fun Content(context: AppContext, state: State, modifier: Modifier = Modifier) {
        when (this) {
            LANDING -> LandingMenu(context, modifier)
            SETTINGS -> SettingsMenu(context, modifier)
            INSPECT_LOCAL -> InspectMenu(context, modifier, remote = false, session_added_songs = state.session_added_songs)
            INSPECT_REMOTE -> InspectMenu(context, modifier, remote = true, session_added_songs = state.session_added_songs)
        }
    }

    companion object {
        val DEFAULT: Menu = LANDING
    }
}
