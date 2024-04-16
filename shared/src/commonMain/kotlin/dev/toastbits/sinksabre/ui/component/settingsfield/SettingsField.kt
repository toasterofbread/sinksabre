package dev.toastbits.sinksabre.ui.component.settingsfield

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text

@Composable
fun SettingsField(
    name: String,
    description: String?,
    oneline: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(name)
                description?.also {
                    Text(it)
                }
            }

            Spacer(Modifier.fillMaxWidth().weight(1f))

            if (oneline) {
                content()
            }
        }

        if (!oneline) {
            content()
        }
    }
}
