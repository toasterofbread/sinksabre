package dev.toastbits.sinksabre.ui.layout

import androidx.compose.runtime.*
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.ui.component.MenuTitleBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import dev.toastbits.sinksabre.platform.localsongs.LocalSongs
import dev.toastbits.composekit.platform.composable.ScrollBarLazyColumn
import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import dev.toastbits.composekit.utils.composable.SubtleLoadingIndicator

@Composable
fun InspectMenu(context: AppContext, modifier: Modifier = Modifier) {
    var local_songs: List<LocalSong>? by remember { mutableStateOf(null) }
    var load_error: Throwable? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        LocalSongs.getLocalSongs(context).fold(
            {
                local_songs = it
                load_error = null
            },
            { load_error = it }
        )
    }

    Column(modifier) {
        MenuTitleBar {
            Text("Inspect songs")
        }

        AlignableCrossfade(
            load_error ?: local_songs,
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
                ScrollBarLazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    items(state as List<LocalSong>) { song ->
                        song.Preview(Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
