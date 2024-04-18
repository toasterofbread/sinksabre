package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.composekit.platform.PlatformFile

expect object LocalSongs {
    suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?>
    suspend fun downloadToLocalSongs(method: SyncMethod, context: AppContext, onProgress: (String) -> Unit): Result<List<PlatformFile>>
}
