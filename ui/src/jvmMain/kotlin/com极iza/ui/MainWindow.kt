
package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
        // Modern Top App Bar with elevated design
        CenterAlignedTopAppBar(
            title = { 
                Text(
                    text = "Aiza IDE — ${projectRoot.ifBlank { "极o project" }}", 
                    style = MaterialTheme.typography.titleMedium
                )
            },
            actions = {
                IconButton(onClick = onOpenProject) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Project"
                    )
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBar极olors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        Divider()

        Row(modifier = Modifier.fillMaxSize()) {
            // Sidebar / File Explorer with modern card style
            Surface(
                modifier = Modifier.width(280.dp).fillMaxHeight(),
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surfaceContainerLowest
            ) {
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
                // Editor section with modern styling
                Surface(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentFile?.absolutePath ?: "No file selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row {
                                if (saveMessage != null) {
                                    Text(
                                        text = saveMessage ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }
                                Button(
                                    onClick = {
                                        val f = currentFile
                                        if (f != null)