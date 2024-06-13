package dev.toastbits.sinksabre.ui.component.settingsfield

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.material3.Switch
import dev.toastbits.sinksabre.settings.Settings
import dev.toastbits.composekit.platform.PreferencesProperty

@Composable
fun BooleanSettingsField(field: PreferencesProperty<Boolean>, modifier: Modifier = Modifier) {
    var state: Boolean by field.observe()

    SettingsField(
        name = field.name,
        description = field.description,
        oneline = true,
        modifier = modifier
    ) {
        Switch(
            state,
            { state = it }
        )
    }
}
