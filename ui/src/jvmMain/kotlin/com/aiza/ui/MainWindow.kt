package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiza.agent.ChatAgent
import kotlinx.coroutines.launch

@Composable
fun App(chatAgent: ChatAgent) {
    MaterialTheme {
        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar / File Explorer Placeholder
            Box(modifier = Modifier.width(200.dp).fillMaxHeight()) {
                Text("File Explorer", modifier = Modifier.padding(16.dp))
            }
            
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight())
            
            // Editor and Chat
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Text("Code Editor Placeholder", modifier = Modifier.padding(16.dp))
                }
                
                Divider(modifier = Modifier.height(1.dp).fillMaxWidth())
                
                Box(modifier = Modifier.height(300.dp)) {
                    ChatView(chatAgent)
                }
            }
        }
    }
}
