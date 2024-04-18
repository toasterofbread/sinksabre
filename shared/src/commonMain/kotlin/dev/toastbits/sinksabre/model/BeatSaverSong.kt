package dev.toatsbits.sinksabre.model

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.runtime.Composable
import dev.toastbits.sinksabre.ui.component.SongPreview
import dev.toastbits.composekit.platform.PlatformFile
import io.kamel.image.asyncPainterResource
import io.kamel.core.Resource

data class BeatSaverSong(
    val id: String,
    val versions: List<Version>,
    override val name: String? = null,
    override val subname: String? = null,
    override val artist_name: String? = null,
    override val mapper_name: String? = null,
    override val bpm: Float? = null
): Song {
    @Composable
    override fun Preview(modifier: Modifier) {
        val image_url: String? =
            if (versions.isNotEmpty()) selectVersion(versions).image_url
            else null

        val painter: Resource<Painter>? = image_url?.let { asyncPainterResource(it) }
        SongPreview(this, modifier, image_painter = painter)
    }

    data class Version(
        val hash: String,
        val download_url: String,
        val image_url: String,
        val preview_url: String
    )

    companion object {
        fun selectVersion(versions: List<Version>): Version {
            require(versions.isNotEmpty())
            return versions.first()
        }
    }
}
