package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Simple settings panel:
 * - Dark theme toggle
 * - API base URL (display-only hint for now)
 * - Placeholders for later settings
 */
@Composable
fun SettingsView(
    darkTheme: Boolean,
    onDarkThemeChange: (Boolean) -> Unit,
    currentProjectRoot: String,
    onClose: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.h5)
        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Dark theme")
            Switch(checked = darkTheme, onCheckedChange = onDarkThemeChange)
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = currentProjectRoot,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            label = { Text("Project root") },
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))
        // Future: allow editing and persisting AIZA_BASE_URL / API key via .env or secure store
        OutlinedTextField(
            value = "Configured via env (.env / GitHub Secrets)",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            label = { Text("AIZA API configuration") },
            singleLine = true
        )

        Spacer(Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onClose) { Text("Close") }
        }
    }
}