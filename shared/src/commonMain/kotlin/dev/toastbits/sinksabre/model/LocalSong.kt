package dev.toatsbits.sinksabre.model

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.runtime.Composable
import dev.toastbits.sinksabre.ui.component.SongPreview
import dev.toastbits.composekit.platform.composable.rememberImagePainter
import dev.toastbits.composekit.platform.PlatformFile

data class LocalSong(
    override val hash: String,
    override val name: String? = null,
    override val subname: String? = null,
    override val artist_name: String? = null,
    override val mapper_name: String? = null,
    override val bpm: Float? = null,
    val image_file: PlatformFile? = null,
    val audio_file: PlatformFile? = null
): Song {
    @Composable
    fun Preview(modifier: Modifier = Modifier) {
        val painter: Painter? = image_file?.let { rememberImagePainter(it.absolute_path) }
        SongPreview(this, modifier, image_painter = painter)
    }
}
