package com.aiza

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.aiza.agent.ChatAgent
import com.aiza.config.Config
import com.aiza.core.AizaApiClient
import com.aiza.ui.MainWindow

fun main() = application {
    val apiClient = AizaApiClient(Config.AIZA_API_KEY, Config.AIZA_BASE_URL)
    val agent = ChatAgent(apiClient)

    Window(onCloseRequest = ::exitApplication, title = "Aiza IDE") {
        MainWindow(agent)
    }
}
