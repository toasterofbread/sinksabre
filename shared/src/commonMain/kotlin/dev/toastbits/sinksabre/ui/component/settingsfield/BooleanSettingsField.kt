package dev.toastbits.sinksabre.ui.component.settingsfield

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.Switch
import dev.toastbits.sinksabre.settings.Settings

@Composable
fun BooleanSettingsField(field: Settings.Field<Boolean>, modifier: Modifier = Modifier) {
    var state: Boolean by field.observe()

    SettingsField(
        name = field.getName(),
        description = field.getDescription(),
        oneline = true,
        modifier = modifier
    ) {
        Switch(
            state,
            { state = it }
        )
    }
}
