package dev.toastbits.sinksabre.ui.layout

import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.ui.component.MenuTitleBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Switch
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.animation.Crossfade
import androidx.compose.animation.AnimatedVisibility
import dev.toastbits.sinksabre.platform.localsongs.LocalSongs
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.composekit.platform.composable.ScrollBarLazyColumn
import dev.toatsbits.sinksabre.model.Song
import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import dev.toastbits.composekit.utils.composable.SubtleLoadingIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun InspectMenu(
    context: AppContext,
    modifier: Modifier = Modifier,
    remote: Boolean = false,
    session_added_songs: List<LocalSong> = emptyList()
) {
    val coroutine_scope: CoroutineScope = rememberCoroutineScope()

    var loaded_songs: List<Song>? by remember { mutableStateOf(null) }
    var load_error: Throwable? by remember { mutableStateOf(null) }
    var reloaded: Boolean by remember { mutableStateOf(false) }
    val sync_method: SyncMethod? by context.settings.SYNC_METHOD.observe()

    var only_show_session_added: Boolean by remember { mutableStateOf(false) }

    fun loadSongs() {
        coroutine_scope.launch {
            loaded_songs = null
            load_error = null

            if (remote) {
                val method: SyncMethod? = sync_method
                if (method?.isConfigured() != true) {
                    load_error = RuntimeException("Sync method not configured")
                    return@launch
                }

                method.getSongList().fold(
                    {
                        loaded_songs = it
                        load_error = null
                    },
                    { load_error = it }
                )
            }
            else {
                LocalSongs.getLocalSongs(context).fold(
                    {
                        loaded_songs = it
                        load_error = null
                    },
                    { load_error = it }
                )
            }
        }
    }

    LaunchedEffect(remote) {
        loadSongs()
        reloaded = false
    }

    Column(modifier) {
        MenuTitleBar(
            buttonContent = {
                IconButton({
                    if (load_error != null || loaded_songs != null) {
                        loadSongs()
                        reloaded = true
                    }
                }) {
                    Crossfade(reloaded && (load_error == null && loaded_songs == null)) { loading ->
                        if (loading) {
                            SubtleLoadingIndicator()
                        }
                        else {
                            Icon(Icons.Default.Refresh, null)
                        }
                    }
                }
            }
        ) {
            if (remote) {
                Text("Inspect remote maps")
            }
            else {
                Text("Inspect local maps")
            }
        }

        AlignableCrossfade(
            load_error ?: if (!remote && only_show_session_added) session_added_songs else loaded_songs,
            Modifier.fillMaxSize().weight(1f),
            contentAlignment = Alignment.Center
        ) { state ->
            if (state == null) {
                SubtleLoadingIndicator()
            }
            else if (state is Throwable) {
                Text(remember(state) { state.stackTraceToString() })
            }
            else if (state is List<*>) {
                if (state.isEmpty()) {
                    Text("Nothing here")
                }
                else {
                    Column {
                        ScrollBarLazyColumn(
                            Modifier.fillMaxSize().weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp),
                            scrollBarColour = LocalContentColor.current
                        ) {
                            items(state as List<Song>) { song ->
                                song.Preview(Modifier.fillMaxWidth())
                            }
                        }

                        CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            color = LocalContentColor.current.copy(alpha = 0.5f)
                        )) {
                            Row(
                                Modifier.padding(top = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${state.size} maps")

                                Spacer(Modifier.fillMaxWidth().weight(1f))

                                AnimatedVisibility(!remote && session_added_songs.isNotEmpty()) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Only maps added this session")

                                        Switch(
                                            only_show_session_added,
                                            { only_show_session_added = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
