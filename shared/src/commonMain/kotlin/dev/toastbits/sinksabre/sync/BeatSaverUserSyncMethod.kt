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
}

private data class BeatSaverUserResponse(
    val id: Int,
    val avatar: String,
    val playlistUrl: String?
)
