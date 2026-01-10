package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiza.agent.EnhancedChatAgent

@Composable
fun App(
    chatAgent: EnhancedChatAgent,
    projectRoot: String,
    onOpenProject: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val terminalOut by chatAgent.terminalOutput.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(text = "Aiza IDE — ${projectRoot.ifBlank { "No project" }}") },
            actions = {
                TextButton(onClick = onOpenProject) { Text("Open") }
                TextButton(onClick = onOpenSettings) { Text("Settings") }
            }
        )

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar / File Explorer
            Box(modifier = Modifier.width(240.dp).fillMaxHeight()) {
                FileExplorerView(rootPath = projectRoot.ifBlank { "." }) { /* TODO: integrate with editor selection */ }
            }

            Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

            // Editor and Chat + Terminal
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text("Code Editor Placeholder", modifier = Modifier.padding(16.dp))
                }

                Divider(modifier = Modifier.height(1.dp).fillMaxWidth())

                Column(modifier = Modifier.height(420.dp).fillMaxWidth()) {
                    Box(modifier = Modifier.height(260.dp).fillMaxWidth()) {
                        ChatView(chatAgent)
                    }
                    Divider(modifier = Modifier.height(1.dp).fillMaxWidth())
                    Box(modifier = Modifier.height(159.dp).fillMaxWidth()) {
                        TerminalView(terminalOut)
                    }
                }
            }
        }
    }
}
