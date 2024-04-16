package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.toatsbits.sinksabre.model.Song
import dev.toastbits.composekit.utils.composable.AlignableCrossfade
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.Alignment
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.LocalTextStyle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.*

@Composable
fun SongPreview(
    song: Song,
    modifier: Modifier = Modifier,
    image_painter: Painter? = null
) {
    val image_size: Dp = 50.dp
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AlignableCrossfade(
            image_painter,
            Modifier.size(image_size),
            contentAlignment = Alignment.Center
        ) { painter ->
            if (painter != null) {
                Image(painter, null)
            }
            else {
                Icon(Icons.Default.QuestionMark, null)
            }
        }

        Column {
            Row {
                Text(song.name ?: "???")

                song.subname?.also {
                    CompositionLocalProvider(
                        LocalTextStyle provides LocalTextStyle.current.copy(
                            color = LocalTextStyle.current.color.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(it)
                    }
                }
            }

            Text("By " + (song.artist_name ?: "???"))
        }
    }
}
