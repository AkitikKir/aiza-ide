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
                
                ChatPanel(chatAgent)
            }
        }
    }
}

@Composable
fun ChatPanel(chatAgent: ChatAgent) {
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("") }
    var chatLog by remember { mutableStateOf("Welcome to Aiza IDE\n") }

    Column(modifier = Modifier.height(300.dp).padding(16.dp)) {
        Text("AI Agent Chat", style = MaterialTheme.typography.h6)
        
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Text(chatLog)
        }
        
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            TextField(
                value = message,
                onValueChange = { message = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message or command...") }
            )
            Button(
                onClick = {
                    val currentMessage = message
                    message = ""
                    chatLog += "You: $currentMessage\n"
                    scope.launch {
                        val response = chatAgent.sendMessage(currentMessage)
                        chatLog += "Agent: $response\n"
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}
