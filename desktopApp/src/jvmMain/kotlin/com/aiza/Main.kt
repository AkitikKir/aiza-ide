package com.aiza

import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aiza.agent.EnhancedChatAgent
import com.aiza.config.Config
import com.aiza.core.AizaApiClient
import com.aiza.ui.AizaTheme
import com.aiza.ui.App
import com.aiza.ui.SettingsView
import com.aiza.ui.WelcomeView

fun main() = application {
    val apiClient = AizaApiClient(Config.AIZA_API_KEY, Config.AIZA_BASE_URL)
    val chatAgent = EnhancedChatAgent(apiClient)

    var darkTheme by remember { mutableStateOf(true) }
    var projectRoot by remember { mutableStateOf("") }
    var showSettings by remember { mutableStateOf(false) }

    Window(onCloseRequest = ::exitApplication, title = "Aiza IDE") {
        AizaTheme(darkTheme = darkTheme) {
            if (projectRoot.isBlank()) {
                // Welcome/select project screen
                WelcomeView(
                    onOpenProject = { selected ->
                        projectRoot = selected
                    }
                )
            } else {
                // Main app
                Surface {
                    App(
                        chatAgent = chatAgent,
                        projectRoot = projectRoot,
                        onOpenProject = { projectRoot = "" },
                        onOpenSettings = { showSettings = true }
                    )
                }

                if (showSettings) {
                    AlertDialog(
                        onDismissRequest = { showSettings = false },
                        title = { Text("Settings") },
                        text = {
                            SettingsView(
                                darkTheme = darkTheme,
                                onDarkThemeChange = { darkTheme = it },
                                currentProjectRoot = projectRoot,
                                onClose = { showSettings = false }
                            )
                        },
                        confirmButton = {},
                        dismissButton = {}
                    )
                }
            }
        }
    }
}
