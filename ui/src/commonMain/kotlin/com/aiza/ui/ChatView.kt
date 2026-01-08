package com.aiza.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aiza.agent.ChatAgent
import com.aiza.core.Message
import kotlinx.coroutines.launch

@Composable
fun ChatView(agent: ChatAgent) {
    val history by agent.history.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(history) { message ->
                MessageItem(message)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Ask Aiza...") }
            )
            Button(
                onClick = {
                    val text = inputText
                    inputText = ""
                    scope.launch {
                        agent.sendMessage(text)
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Send")
            }
        }
    }
}

@Composable
fun MessageItem(message: Message) {
    val bgColor = if (message.role == "user") Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
    val alignment = if (message.role == "user") Alignment.End else Alignment.Start

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = alignment) {
        Card(backgroundColor = bgColor, elevation = 2.dp) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = message.role.uppercase(), style = MaterialTheme.typography.caption)
                Text(text = message.content)
            }
        }
    }
}
