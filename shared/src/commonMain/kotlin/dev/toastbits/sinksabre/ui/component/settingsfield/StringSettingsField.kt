package dev.toastbits.sinksabre.ui.component.settingsfield

import androidx.compose.runtime.*
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import dev.toastbits.sinksabre.settings.Settings

@Composable
fun StringSettingsField(field: Settings.Field<String>, modifier: Modifier = Modifier) {
    var state: String by field.observe()

    SettingsField(
        name = field.getName(),
        description = field.getDescription(),
        oneline = true,
        modifier = modifier
    ) {
        TextField(
            state,
            { state = it },
            singleLine = true
        )
    }
}
