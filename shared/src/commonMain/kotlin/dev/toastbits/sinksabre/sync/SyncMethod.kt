package dev.toastbits.sinksabre.sync

import kotlinx.serialization.Serializable
import dev.toatsbits.sinksabre.model.Song
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.composekit.utils.composable.OnChangedEffect
import dev.toastbits.composekit.platform.PlatformFile
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import androidx.compose.runtime.*

@Serializable
sealed interface SyncMethod {
    fun getType(): Type
    fun isConfigured(): Boolean

    @Composable
    fun ConfigurationItems(context: AppContext, onModification: (SyncMethod) -> Unit)

    suspend fun downloadSongs(
        directory: PlatformFile,
        onProgress: (String) -> Unit
    ): Result<List<PlatformFile>>

    fun canUploadSongs(): Boolean = false
    suspend fun uploadSongs(
        songs: List<Song>,
        onProgress: (String) -> Unit
    ): Result<Unit> = Result.failure(IllegalAccessException())

    enum class Type {
        BEAT_SAVER_USER;

        fun create(): SyncMethod =
            when (this) {
                BEAT_SAVER_USER -> BeatSaverUserSyncMethod()
            }
    }

    suspend fun getClient(): HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
        }

    companion object {
        @Composable
        fun observe(context: AppContext): MutableState<SyncMethod?> {
            var data: String by context.settings.SYNC_METHOD.observe()
            val state: MutableState<SyncMethod?> = remember { mutableStateOf(data.decodeSyncMethod()) }
            var set_to: SyncMethod? by remember { mutableStateOf(state.value) }

            LaunchedEffect(state.value) {
                if (state.value != set_to) {
                    set_to = state.value
                    data = set_to?.let { Json.encodeToString(it) } ?: ""
                }
            }

            OnChangedEffect(data) {
                set_to = data.decodeSyncMethod()
                state.value = set_to
            }

            return state
        }
    }
}

private fun String.decodeSyncMethod(): SyncMethod? =
    try {
        Json.decodeFromString<SyncMethod>(this)
    }
    catch (_: Throwable) {
        null
    }

fun SyncMethod.Type?.getName(): String =
    when (this) {
        SyncMethod.Type.BEAT_SAVER_USER -> "BeatSaver user"
        null -> "None"
    }
