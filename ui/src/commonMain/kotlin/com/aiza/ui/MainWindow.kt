package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aiza.agent.ChatAgent
import java.io.File

@Composable
fun MainWindow(agent: ChatAgent) {
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var editorContent by remember { mutableStateOf("") }
    var terminalOutput by remember { mutableStateOf("Welcome to Aiza IDE Terminal\n$ ") }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Aiza IDE") })
            }
        ) { padding ->
            Row(modifier = Modifier.fillMaxSize().padding(padding)) {
                // Sidebar: File Explorer
                Box(modifier = Modifier.width(250.dp).fillMaxHeight()) {
                    FileExplorerView(rootPath = ".", onFileSelected = { file ->
                        selectedFile = file
                        if (!file.isDirectory) {
                            editorContent = file.readText()
                        }
                    })
                }

                Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

                // Main Content: Editor and Terminal
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    Box(modifier = Modifier.weight(0.7f).fillMaxWidth()) {
                        CodeEditorView(content = editorContent, onContentChange = { editorContent = it })
                    }
                    Divider(modifier = Modifier.height(1.dp).fillMaxWidth())
                    Box(modifier = Modifier.weight(0.3f).fillMaxWidth()) {
                        TerminalView(output = terminalOutput)
                    }
                }

                Divider(modifier = Modifier.width(1.dp).fillMaxHeight())

                // Right Panel: Chat Agent
                Box(modifier = Modifier.width(350.dp).fillMaxHeight()) {
                    ChatView(agent = agent)
                }
            }
        }
    }
}
