package dev.toastbits.sinksabre.sync

import kotlinx.serialization.Serializable
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import dev.toastbits.sinksabre.ui.component.settingsfield.StringSettingsField
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.composekit.utils.composable.OnChangedEffect
import dev.toatsbits.sinksabre.model.Song
import dev.toastbits.sinksabre.settings.Settings

@Serializable
data class BeatSaverUserSyncMethod(val username: String = ""): SyncMethod {
    override fun getType(): SyncMethod.Type = SyncMethod.Type.BEAT_SAVER_USER

    override fun isConfigured(): Boolean = username.isNotBlank()

    @Composable
    override fun ConfigurationItems(context: AppContext, onModification: (SyncMethod) -> Unit) {
        StringSettingsField(
            remember(this) {
                object : Settings.Field<String> {
                    override fun get(): String = username
                    override fun set(value: String) = onModification(copy(username = value))

                    override fun getName(): String = "Username"
                    override fun getDescription(): String? = null

                    @Composable
                    override fun observe(): MutableState<String> {
                        val state: MutableState<String> = remember(this) { mutableStateOf(username) }
                        var set_to: String by remember(this) { mutableStateOf(username) }

                        LaunchedEffect(state.value) {
                            if (state.value != set_to) {
                                set_to = state.value
                                set(set_to)
                            }
                        }

                        return state
                    }
                }
            }
        )
    }

    override suspend fun getSongList(): Result<List<Song>> = runCatching {
        val client: HttpClient = getClient()

        val response: HttpResponse = client.get("https://api.beatsaver.com/users/name/burnerofbread")
        val result: BeatSaverUserResponse = response.body()

        TODO(result.toString())
    }

    override suspend fun downloadSongs(
        songs: List<Song>,
        directory: PlatformFile,
        onProgress: (String) -> Unit
    ): Result<List<PlatformFile>> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting map download URLs")
        val maps: List<BeatSaverMapResponse> = client.getSongDownloadUrls(songs).filter { it.versions.isNotEmpty() }
        
        for ((index, map) in maps.withIndex()) {
            onProgress("Downloading ${map.name} by ${map.uploader.name} (${index + 1} / ${maps.size})")

            val response: HttpResponse = client.get(map.versions.first().downloadURL)
            // TODO extract with java.util.zip.ZipInputStream
        }

        TODO()
    }

    private suspend fun HttpClient.getSongDownloadUrls(songs: List<Song>): List<BeatSaverMapResponse> {
        return songs.chunked(50).flatMap { chunk_songs ->
            val ids: String = chunk_songs.joinToString(",")
            val result: Map<String, BeatSaverMapResponse> = get("https://api.beatsaver.com/maps/ids/$ids").body()
            return@flatMap result.values
        }
    }
}

@Serializable
private data class BeatSaverUserResponse(
    val id: Int,
    val name: String,
    val avatar: String
)

@Serializable
private data class BeatSaverMapResponse(
    val id: String,
    val name: String,
    val description: String,
    val uploader: BeatSaverUserResponse,
    val versions: List<Version>
) {
    @Serializable
    data class Version(val downloadURL: String)
}
