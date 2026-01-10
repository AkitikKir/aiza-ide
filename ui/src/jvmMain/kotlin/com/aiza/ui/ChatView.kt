package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.aiza.agent.EnhancedChatAgent
import com.aiza.agent.ToolRequest
import com.aiza.core.Message
import kotlinx.coroutines.launch
import kotlin.math.max
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@Composable
fun ChatView(agent: EnhancedChatAgent) {
    val history by agent.history.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var isStreaming by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Autoscroll to bottom on new messages
    LaunchedEffect(history.size) {
        if (history.isNotEmpty()) {
            listState.animateScrollToItem(max(0, history.size - 1))
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Pending approvals UI
        val approvals: Map<String, ToolRequest> = agent.getPendingApprovals()
        if (approvals.isNotEmpty()) {
            Card(
                backgroundColor = Color(0xFFFFF8E1),
                elevation = 2.dp,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        "Pending approvals",
                        style = MaterialTheme.typography.subtitle2,
                        color = Color(0xFF6D4C41)
                    )
                    Spacer(Modifier.height(4.dp))
                    approvals.forEach { (id, req) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text("• ${req.tool} ${req.parameters}", modifier = Modifier.weight(1f))
                            TextButton(onClick = { scope.launch { agent.approveCommand(id) } }) {
                                Text("Approve")
                            }
                            TextButton(onClick = { agent.denyCommand(id) }) {
                                Text("Deny")
                            }
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), state = listState) {
            items(history) { message ->
                MessageItem(message, agent)
            }
        }

        if (isStreaming) {
            Text(
                "Assistant is typing…",
                style = MaterialTheme.typography.caption,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .onPreviewKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown &&
                            event.key == Key.Enter &&
                            (event.isCtrlPressed || event.isMetaPressed)
                        ) {
                            val text = inputText
                            if (text.isNotBlank()) {
                                inputText = ""
                                isStreaming = true
                                scope.launch {
                                    try {
                                        agent.sendMessageStreaming(text)
                                    } finally {
                                        isStreaming = false
                                    }
                                }
                            }
                            true
                        } else {
                            false
                        }
                    },
                placeholder = { Text("Ask Aiza...") }
            )
            Button(
                onClick = {
                    val text = inputText
                    if (text.isNotBlank()) {
                        inputText = ""
                        isStreaming = true
                        scope.launch {
                            try {
                                agent.sendMessageStreaming(text)
                            } finally {
                                isStreaming = false
                            }
                        }
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
fun MessageItem(message: Message, agent: EnhancedChatAgent) {
    val bgColor = if (message.role == "user") Color(0xFFE3F2FD) else Color(0xFFF5F5F5)
    val alignment = if (message.role == "user") Alignment.End else Alignment.Start
    val scope = rememberCoroutineScope()
    val blocks = remember(message.content) { parseCodeBlocks(message.content) }
    val plainText = remember(message.content) { stripCodeBlocks(message.content).trim() }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalAlignment = alignment) {
        Card(backgroundColor = bgColor, elevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(text = message.role.uppercase(), style = MaterialTheme.typography.caption)
                if (plainText.isNotEmpty()) {
                    Text(text = plainText, modifier = Modifier.padding(top = 2.dp))
                }

                // Render code blocks with actions
                blocks.forEach { block ->
                    Spacer(Modifier.height(8.dp))
                    Card(backgroundColor = Color(0xFF121212), elevation = 1.dp) {
                        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = (block.lang ?: "text").uppercase(),
                                    color = Color(0xFFB0BEC5),
                                    style = MaterialTheme.typography.caption
                                )
                                Row {
                                    TextButton(onClick = { copyToClipboard(block.code) }) {
                                        Text("Copy code", color = Color(0xFF90CAF9))
                                    }
                                    if (block.isRunnable()) {
                                        TextButton(onClick = {
                                            scope.launch { agent.requestShell(block.code) }
                                        }) {
                                            Text("Run in Terminal", color = Color(0xFF81C784))
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = block.code,
                                color = Color(0xFFE0E0E0),
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Per-message actions
                Row(modifier = Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { copyToClipboard(message.content) }) {
                        Text("Copy message")
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(text: String) {
    runCatching {
        val selection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, selection)
    }
}

private data class CodeBlock(val lang: String?, val code: String)

private fun CodeBlock.isRunnable(): Boolean {
    val l = (lang ?: "").lowercase()
    return l in setOf("sh", "bash", "zsh", "shell")
}

private fun parseCodeBlocks(content: String): List<CodeBlock> {
    val blocks = mutableListOf<CodeBlock>()
    val regex = Regex("```(\\w+)?\\s*([\\s\\S]*?)\\s*```", RegexOption.MULTILINE)
    regex.findAll(content).forEach { m ->
        val lang = m.groups[1]?.value
        val code = m.groups[2]?.value ?: ""
        blocks.add(CodeBlock(lang, code))
    }
    return blocks
}

private fun stripCodeBlocks(content: String): String {
    val regex = Regex("```(\\w+)?\\s*[\\s\\S]*?\\s*```", RegexOption.MULTILINE)
    return content.replace(regex, "")
}
