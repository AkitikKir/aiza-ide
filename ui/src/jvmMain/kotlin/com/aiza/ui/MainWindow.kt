package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiza.agent.EnhancedChatAgent

@Composable
fun App(chatAgent: EnhancedChatAgent) {
    MaterialTheme {
        val terminalOut by chatAgent.terminalOutput.collectAsState()

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar / File Explorer
            Box(modifier = Modifier.width(240.dp).fillMaxHeight()) {
                FileExplorerView(rootPath = ".") { /* TODO: integrate with editor selection */ }
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
