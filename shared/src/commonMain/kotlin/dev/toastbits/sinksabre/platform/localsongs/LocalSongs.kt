package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext

expect object LocalSongs {
    suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?>
}
