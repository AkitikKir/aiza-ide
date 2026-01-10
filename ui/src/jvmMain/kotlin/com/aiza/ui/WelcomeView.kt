package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File
import java.awt.GraphicsEnvironment
import javax.swing.JFileChooser
import javax.swing.SwingUtilities

@Composable
fun WelcomeView(
    onOpenProject: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var path by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(max = 720.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to Aiza IDE", style = MaterialTheme.typography.h4)
            Spacer(Modifier.height(8.dp))
            Text("Select a project directory to start", style = MaterialTheme.typography.subtitle1)
            Spacer(Modifier.height(24.dp))

            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = path,
                        onValueChange = {
                            path = it
                            error = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Project path") },
                        singleLine = true
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            val chosen = safeChooseDirectory(initial = path)
                            if (chosen != null) {
                                path = chosen
                                error = null
                            } else {
                                error = "Browse is not supported in this environment. Enter path manually."
                            }
                        }) { Text("Browse…") }
                        Row {
                            TextButton(onClick = {
                                val e = validateProjectDir(path)
                                if (e != null) {
                                    error = e
                                } else {
                                    // Ensure directory exists
                                    val f = File(path)
                                    runCatching { if (!f.exists()) f.mkdirs() }
                                    error = null
                                    onOpenProject(path)
                                }
                            }) { Text("Open") }
                        }
                    }

                    if (error != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(error ?: "", color = MaterialTheme.colors.error)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Quick shortcuts
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    val chosen = safeChooseDirectory(initial = path)
                    if (chosen != null) {
                        path = chosen
                        val e = validateProjectDir(path)
                        if (e == null) onOpenProject(path) else error = e
                    } else {
                        error = "Browse is not supported in this environment. Enter path manually."
                    }
                }) { Text("Open Folder") }
            }
        }
    }
}

/**
 * Attempts to open a Swing directory chooser. Returns null if headless/unsupported
 * or the user cancels.
 */
private fun safeChooseDirectory(initial: String): String? {
    // If running headless (CI or environments without a display), skip Swing chooser
    if (runCatching { GraphicsEnvironment.isHeadless() }.getOrDefault(true)) {
        return null
    }
    return runCatching {
        var selected: String? = null
        // Run on Swing EDT; if Swing not available, this throws and we fallback.
        SwingUtilities.invokeAndWait {
            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                if (initial.isNotBlank()) {
                    val start = File(initial)
                    currentDirectory = if (start.exists()) {
                        if (start.isDirectory) start else start.parentFile
                    } else {
                        currentDirectory
                    }
                }
            }
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                selected = chooser.selectedFile.absolutePath
            }
        }
        selected
    }.getOrNull()
}

private fun validateProjectDir(path: String): String? {
    if (path.isBlank()) return "Path cannot be empty"
    val f = File(path)
    if (!f.exists() || !f.isDirectory) return "Path must be an existing directory"
    // Optionally check for Gradle markers; allow any folder for now
    return null
}