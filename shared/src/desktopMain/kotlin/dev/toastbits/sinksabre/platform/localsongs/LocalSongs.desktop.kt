package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext

actual object LocalSongs {
    actual suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?> = runCatching {
        return@runCatching listOf(LocalSong("Hello World!"))
    }
}
