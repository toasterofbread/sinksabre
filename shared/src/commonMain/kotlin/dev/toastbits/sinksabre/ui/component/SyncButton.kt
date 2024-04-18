package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.*
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.foundation.layout.padding
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
import dev.toastbits.composekit.utils.composable.SubtleLoadingIndicator
import dev.toastbits.composekit.platform.PlatformFile
import dev.toastbits.composekit.platform.composable.ScrollBarLazyColumn
import dev.toatsbits.sinksabre.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

enum class SyncType {
    DOWNLOAD, UPLOAD;

    fun isAvailable(method: SyncMethod?): Boolean =
        when (this) {
            DOWNLOAD -> true
            UPLOAD -> method?.canUploadSongs() == true
        }

    fun getLabel(): String =
        when (this) {
            DOWNLOAD -> "Download"
            UPLOAD -> "Upload"
        }

    fun getColour(): Color =
        when (this) {
            DOWNLOAD -> Color(0xFFE5446D)
            UPLOAD -> Color(0xFFE5446D)
        }

    fun getIcon(): ImageVector =
        when (this) {
            DOWNLOAD -> Icons.Default.Refresh
            UPLOAD -> Icons.Default.CloudUpload
        }
}

@Composable
fun SyncButton(
    context: AppContext,
    modifier: Modifier = Modifier,
    onShowingContentChanged: (Boolean) -> Unit = {}
) {
    val coroutine_scope: CoroutineScope = rememberCoroutineScope()
    val outer_content_colour: Color = LocalContentColor.current

    val sync_method: SyncMethod? by SyncMethod.observe(context)
    var sync_error: Throwable? by remember { mutableStateOf(null) }

    var syncing: Boolean by remember { mutableStateOf(false) }
    var current_sync_type: SyncType? by remember { mutableStateOf(null) }

    var sync_log: String by remember { mutableStateOf("") }

    val showing_content: Boolean by remember { derivedStateOf {
        current_sync_type != null || sync_error != null || sync_log.isNotEmpty()
    } }

    LaunchedEffect(showing_content) {
        onShowingContentChanged(showing_content)
    }

    fun onProgress(message: String) {
        println(message)
        sync_log += "$message\n"
    }

    fun performSync(type: SyncType) {
        if (syncing) {
            return
        }

        if (sync_error != null) {
            sync_error = null
            return
        }

        if (sync_log.isNotEmpty()) {
            sync_log = ""
            current_sync_type = null
            return
        }

        val method: SyncMethod =
            sync_method?.takeIf { it.isConfigured() }
            ?: return

        syncing = true
        current_sync_type = type

        coroutine_scope.launch {
            try {
                when (type) {
                    SyncType.DOWNLOAD ->
                        LocalSongs.downloadToLocalSongs(method, context, { onProgress(it) }).getOrThrow()

                    SyncType.UPLOAD -> {
                        onProgress("Loading local songs")

                        val local_songs: List<Song> = LocalSongs.getLocalSongs(context).getOrThrow() ?: emptyList()
                        method.uploadSongs(local_songs, { onProgress(it) }).getOrThrow()
                    }
                }

                sync_error = null
            }
            catch (e: Throwable) {
                val error: Throwable =
                    RuntimeException("Song sync failed for method '$method' and type '$type' on step '${sync_log.split("\n").lastOrNull()}'", e)
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
            performSync(SyncType.DOWNLOAD)
        }
    }

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val fill_weight: Float by animateFloatAsState(if (current_sync_type == null) 1f else Float.MAX_VALUE)

        for (type in SyncType.entries) {
            AnimatedVisibility(
                type.isAvailable(sync_method) && (!syncing || current_sync_type == type),
                Modifier.fillMaxWidth().weight(if (current_sync_type == type) fill_weight else 1f),
                exit = shrinkHorizontally()
            ) {
                BigButton(
                    {
                        performSync(type)
                    },
                    type.getColour(),
                    icon = type.getIcon(),
                    full_content = showing_content,
                    enabled = sync_method?.isConfigured() == true || sync_error != null || sync_log.isNotEmpty(),
                    disabledContent = {
                        Text("Sync method not configured")
                    }
                ) {
                    AlignableCrossfade(
                        sync_error ?: if (syncing) true else if (current_sync_type == type) sync_log.takeIf { it.isNotEmpty() } else null,
                        contentAlignment = Alignment.Center
                    ) { state ->
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
                            SyncLog(sync_log, true, Modifier.fillMaxSize())
                        }
                        else if (state is String) {
                            SyncLog(state, false, Modifier.fillMaxSize()) {
                                Column(
                                    Modifier
                                        .padding(top = 10.dp)
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
                            Text(type.getLabel())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncLog(
    text: String,
    running: Boolean,
    modifier: Modifier = Modifier,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val lines: List<String> = remember(text) { text.split("\n") }
    val last_empty_line: Int = remember(lines) { lines.indexOfLast { it.isNotBlank() } }

    val list_state: LazyListState = rememberLazyListState()

    LaunchedEffect(lines.size, bottomContent != null) {
        list_state.animateScrollToItem(list_state.layoutInfo.totalItemsCount)
    }

    ScrollBarLazyColumn(
        modifier,
        state = list_state
    ) {
        itemsIndexed(lines) { index, line ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    line,
                    textAlign = TextAlign.Start,
                    fontSize = 20.sp
                )

                if (running && index == last_empty_line) {
                    SubtleLoadingIndicator(Modifier.alpha(0.5f))
                }
            }
        }

        if (bottomContent != null) {
            item {
                bottomContent.invoke()
            }
        }
    }
}
