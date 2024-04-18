package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.composekit.platform.PlatformFile

actual object LocalSongs {
    actual suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?> = runCatching {
        return@runCatching listOf(LocalSong("27171ced59d73d2095f36ca22c708a841d69df4f"))
    }

    actual suspend fun downloadToLocalSongs(
        method: SyncMethod,
        context: AppContext,
        onProgress: (String) -> Unit
    ): Result<List<PlatformFile>> {
        return method.downloadSongs(PlatformFile(context.getFilesDir().resolve("songs")), onProgress)
    }
}
