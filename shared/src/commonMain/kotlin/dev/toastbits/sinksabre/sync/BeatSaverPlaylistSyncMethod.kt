package dev.toastbits.sinksabre.sync

import dev.toastbits.sinksabre.sync.beatsaver.*
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.ui.component.settingsfield.StringSettingsField
import dev.toastbits.sinksabre.settings.Settings
import dev.toastbits.composekit.platform.PlatformFile
import dev.toastbits.composekit.platform.PreferencesProperty
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toatsbits.sinksabre.model.Song
import dev.toatsbits.sinksabre.model.LocalSong
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import androidx.compose.runtime.*
import io.ktor.client.HttpClient

@Serializable
data class BeatSaverPlaylistSyncMethod(
    val playlist_id: Int? = null
): SyncMethod {
    override fun getType(): SyncMethod.Type = SyncMethod.Type.BEAT_SAVER_PLAYLIST

    override fun isConfigured(): Boolean = playlist_id != null

    @Composable
    override fun ConfigurationItems(context: AppContext, onModification: (SyncMethod) -> Unit) {
        StringSettingsField(
            remember(this) {
                object : PreferencesProperty<String> {
                    override fun get(): String = playlist_id?.toString() ?: ""

                    override fun set(value: String, editor: PlatformPreferences.Editor?) {
                        onModification(copy(playlist_id = value.toIntOrNull()))
                    }
                    override fun set(data: JsonElement, editor: PlatformPreferences.Editor?) {
                        set(data.jsonPrimitive.content, editor)
                    }

                    override fun getDefaultValue(): String = ""
                    override fun reset() {
                        set(getDefaultValue())
                    }

                    override val key: String = ""
                    override val name: String = "Playlist ID"
                    override val description: String? = null

                    override fun serialise(value: Any?): JsonElement =
                        JsonPrimitive(value as String?)

                    @Composable
                    override fun observe(): MutableState<String> {
                        val state: MutableState<String> = remember(this) { mutableStateOf(playlist_id?.toString() ?: "") }
                        var set_to: String by remember(this) { mutableStateOf(playlist_id?.toString() ?: "") }

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
        val maps: List<BeatSaverMap> = client.getPlaylistMaps(playlist_id!!, null)
        return@runCatching maps.map { it.toSong() }
    }

    override suspend fun downloadSongs(
        directory: PlatformFile,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting playlist maps")
        val all_maps: List<BeatSaverMap> = client.getPlaylistMaps(playlist_id!!, null)

        val maps: List<BeatSaverMap> = all_maps.filter { map ->
            for (version in map.versions) {
                val files: List<PlatformFile> = directory.resolve(version.hash).listFiles() ?: emptyList()
                if (files.any { it.name.endsWith(".zip") } || files.isEmpty()) {
                    continue
                }

                return@filter false
            }

            return@filter true
        }

        onProgress("Downloading ${maps.size} maps (skipping ${all_maps.size - maps.size} already downloaded)")
        onFractionalProgress(0f)

        return@runCatching client.downloadBeatSaverMaps(
            maps,
            directory,
            onFractionalProgress,
            onProgress
        )
    }

    override fun canUploadSongs(): Boolean = false
}
