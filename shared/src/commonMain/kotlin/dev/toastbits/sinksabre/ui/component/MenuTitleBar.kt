package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor

@Composable
fun MenuTitleBar(
    modifier: Modifier = Modifier,
    buttonContent: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null
) {
    Row(
        modifier.padding(bottom = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            val title_style: TextStyle = MaterialTheme.typography.headlineLarge
            val content_style: TextStyle = MaterialTheme.typography.bodyLarge

            CompositionLocalProvider(LocalTextStyle provides title_style) {
                Text("SinkSabre")
            }

            CompositionLocalProvider(
                LocalTextStyle provides content_style,
                LocalContentColor provides LocalContentColor.current.copy(alpha = 0.75f)
            ) {
                content?.invoke()
            }
        }

        Spacer(Modifier.fillMaxWidth().weight(1f))

        buttonContent?.invoke()
    }
}
