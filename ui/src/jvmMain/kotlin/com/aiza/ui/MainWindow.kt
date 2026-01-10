package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiza.agent.EnhancedChatAgent
import java.io.File

@Composable
fun App(
    chatAgent: EnhancedChatAgent,
    projectRoot: String,
    onOpenProject: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val terminalOut by chatAgent.terminalOutput.collectAsState()

    // Editor state
    var currentFile by remember { mutableStateOf<File?>(null) }
    var editorText by remember { mutableStateOf("") }
    var saveMessage by remember { mutableStateOf<String?>(null) }

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
                FileExplorerView(rootPath = projectRoot.ifBlank { "." }) { file ->
                    if (file.isFile) {
                        currentFile = file
                        editorText = runCatching { file.readText() }.getOrElse { "" }
                        saveMessage = null
                    }
                }
            }

            Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

            // Editor and Chat + Terminal
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = currentFile?.absolutePath ?: "No file selected",
                                style = MaterialTheme.typography.caption
                            )
                            Row {
                                if (saveMessage != null) {
                                    Text(
                                        text = saveMessage ?: "",
                                        style = MaterialTheme.typography.caption,
                                        color = MaterialTheme.colors.secondary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }
                                Button(
                                    onClick = {
                                        val f = currentFile
                                        if (f != null) {
                                            val result = runCatching { f.writeText(editorText) }
                                            saveMessage = if (result.isSuccess) "Saved" else "Save failed"
                                        } else {
                                            saveMessage = "No file to save"
                                        }
                                    },
                                    enabled = currentFile != null
                                ) { Text("Save") }
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 6.dp))
                        CodeEditorView(
                            content = editorText,
                            onContentChange = { editorText = it }
                        )
                    }
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
