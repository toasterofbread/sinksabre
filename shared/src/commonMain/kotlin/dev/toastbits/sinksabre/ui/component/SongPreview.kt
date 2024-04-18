package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.toatsbits.sinksabre.model.Song
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.*
import io.kamel.core.Resource
import io.kamel.image.KamelImage

@Composable
fun SongPreview(
    song: Song,
    modifier: Modifier = Modifier,
    image_painter: Resource<Painter>? = null
) {
    val image_size: Dp = 50.dp
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AlignableCrossfade(
            image_painter,
            Modifier.size(image_size),
            contentAlignment = Alignment.Center
        ) { painter ->
            if (painter != null) {
                KamelImage(
                    painter,
                    null,
                    onFailure = { it.printStackTrace() }
                )
            }
            else {
                Icon(Icons.Default.QuestionMark, null)
            }
        }

        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(song.name ?: "???", softWrap = false, maxLines = 1)

                song.subname?.also {
                    Text(it, softWrap = false, maxLines = 1, color = LocalContentColor.current.copy(alpha = 0.5f))
                }
            }

            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(
                color = LocalContentColor.current.copy(alpha = 0.5f),
                fontSize = 10.sp
            )) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    song.artist_name?.also { artist ->
                        Text("Artist: $artist", softWrap = false, maxLines = 1)
                    }
                    song.mapper_name?.also { mapper ->
                        Text("Mapper: $mapper", softWrap = false, maxLines = 1)
                    }
                }
            }
        }
    }
}
