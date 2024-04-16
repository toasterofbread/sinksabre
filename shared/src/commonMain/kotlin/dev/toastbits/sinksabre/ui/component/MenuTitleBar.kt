package dev.toastbits.sinksabre.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.LocalContentColor

@Composable
fun MenuTitleBar(
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    Column(modifier.padding(bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        val title_style: TextStyle = MaterialTheme.typography.headlineLarge
        val content_style: TextStyle =
            MaterialTheme.typography.bodyLarge.copy(color = LocalContentColor.current.copy(alpha = 0.75f))

        CompositionLocalProvider(LocalTextStyle provides title_style) {
            Text("SinkSabre")
        }

        CompositionLocalProvider(LocalTextStyle provides content_style) {
            content?.invoke()
        }
    }
}
