package dev.toastbits.sinksabre.ui.component.settingsfield

import androidx.compose.runtime.*
import androidx.compose.material3.TextField
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.material.icons.Icons
import dev.toastbits.sinksabre.settings.Settings
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon

@Composable
fun StringSettingsField(
    field: Settings.Field<String>,
    modifier: Modifier = Modifier,
    censorable: Boolean = false
) {
    var state: String by field.observe()
    var censored: Boolean by remember { mutableStateOf(censorable) }

    SettingsField(
        name = field.getName(),
        description = field.getDescription(),
        oneline = true,
        modifier = modifier
    ) {
        TextField(
            state,
            { state = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType =
                    if (censored) KeyboardType.Password
                    else KeyboardType.Text
            ),
            visualTransformation =
                if (censored) PasswordVisualTransformation()
                else VisualTransformation.None,
            trailingIcon = {
                AnimatedVisibility(censorable) {
                    IconButton({ censored = !censored }) {
                        Crossfade(censored) {
                            Icon(
                                if (it) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                null
                            )
                        }
                    }
                }
            }
        )
    }
}
