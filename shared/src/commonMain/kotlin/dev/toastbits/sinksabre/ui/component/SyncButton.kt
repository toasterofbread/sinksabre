package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import dev.toastbits.sinksabre.ui.component.BigButton
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.platform.localsongs.LocalSongs
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import dev.toastbits.composekit.utils.composable.NullCrossfade
import dev.toastbits.composekit.platform.PlatformFile
import dev.toatsbits.sinksabre.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun SyncButton(
    context: AppContext,
    modifier: Modifier = Modifier,
    onShowingContentChanged: (Boolean) -> Unit = {}
) {
    val coroutine_scope: CoroutineScope = rememberCoroutineScope()
    val outer_content_colour: Color = LocalContentColor.current

    val sync_method: SyncMethod? by SyncMethod.observe(context)
    var syncing: Boolean by remember { mutableStateOf(false) }
    var sync_error: Throwable? by remember { mutableStateOf(null) }

    var sync_log: String by remember { mutableStateOf("") }

    val showing_content: Boolean by remember { derivedStateOf {
        syncing || sync_error != null || sync_log.isNotEmpty()
    } }

    LaunchedEffect(showing_content) {
        onShowingContentChanged(showing_content)
    }

    fun performSync() {
        if (syncing) {
            return
        }

        if (sync_error != null) {
            sync_error = null
            return
        }

        if (sync_log.isNotEmpty()) {
            sync_log = ""
            return
        }

        val method: SyncMethod =
            sync_method?.takeIf { it.isConfigured() }
            ?: return

        coroutine_scope.launch {
            syncing = true

            try {
                val added_maps: List<PlatformFile> =
                    LocalSongs.syncToLocalSongs(
                        method,
                        context,
                        onProgress = { message ->
                            println(message)
                            sync_log += "\n$message"
                        }
                    ).getOrThrow()

                println(added_maps.toList())

                sync_error = null
            }
            catch (e: Throwable) {
                val error: Throwable =
                    RuntimeException("Song sync failed for method '$method' on step '${sync_log.split("\n").lastOrNull()}'", e)
                sync_error = error
                error.printStackTrace()
                sync_log = ""
            }
            finally {
                syncing = false
            }
        }
    }

    LaunchedEffect(Unit) {
        if (context.settings.SYNC_ON_START.get()) {
            performSync()
        }
    }

    BigButton(
        {
            performSync()
        },
        Color(0xFFE5446D),
        modifier,
        icon = Icons.Default.Refresh,
        full_content = showing_content,
        enabled = sync_method?.isConfigured() == true || sync_error != null || sync_log.isNotEmpty(),
        disabledContent = {
            Text("Sync method not configured")
        }
    ) {
        AlignableCrossfade(sync_error ?: if (syncing) true else sync_log.takeIf { it.isNotEmpty() }, contentAlignment = Alignment.Center) { state ->
            if (state is Throwable) {
                Column {
                    Text("Sync failed")
                    Text(
                        remember(state) { state.stackTraceToString() },
                        Modifier
                            .horizontalScroll(rememberScrollState())
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
            else if ((state as? Boolean) == true) {
                SyncLog(sync_log, Modifier.fillMaxSize())
            }
            else if (state is String) {
                Column(
                    Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SyncLog(state, Modifier.fillMaxWidth())

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .background(outer_content_colour.getContrasted()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Sync completed",
                            color = outer_content_colour
                        )
                        Text(
                            "Press to dimsiss",
                            color = outer_content_colour.copy(alpha = 0.5f),
                            fontSize = 15.sp
                        )
                    }
                }
            }
            else {
                Text("Sync")
            }
        }
    }
}

@Composable
private fun SyncLog(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier,
        textAlign = TextAlign.Start,
        fontSize = 20.sp
    )
}
