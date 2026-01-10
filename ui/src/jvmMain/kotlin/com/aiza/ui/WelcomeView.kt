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
            modifier = Modifier.align(Alignment.Center).widthIn(max = 720.dp).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome to Aiza IDE",
                style = MaterialTheme.typography.h4
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Select a project directory to start",
                style = MaterialTheme.typography.subtitle1
            )
            Spacer(Modifier.height(24.dp))

            Card(elevation = 4.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = path,
                        onValueChange = { path = it },
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
                            val chosen = chooseDirectory(initial = path)
                            if (chosen != null) {
                                path = chosen
                                error = null
                            }
                        }) { Text("Browse…") }
                        Row {
                            TextButton(onClick = {
                                // quick create empty folder if not exists
                                val f = File(path)
                                runCatching { if (!f.exists()) f.mkdirs() }
                                error = validateProjectDir(path)
                                if (error == null) {
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
                    val chosen = chooseDirectory(initial = path)
                    if (chosen != null) {
                        path = chosen
                        error = validateProjectDir(path)
                        if (error == null) onOpenProject(path)
                    }
                }) { Text("Open Folder") }
            }
        }
    }
}

private fun chooseDirectory(initial: String): String? {
    var selected: String? = null
    // Ensure chooser runs on Swing EDT
    SwingUtilities.invokeAndWait {
        val chooser = JFileChooser()
        chooser.fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        if (initial.isNotBlank()) {
            val start = File(initial)
            if (start.exists()) chooser.currentDirectory = if (start.isDirectory) start else start.parentFile
        }
        val result = chooser.showOpenDialog(null)
        if (result == JFileChooser.APPROVE_OPTION) {
            selected = chooser.selectedFile.absolutePath
        }
    }
    return selected
}

private fun validateProjectDir(path: String): String? {
    if (path.isBlank()) return "Path cannot be empty"
    val f = File(path)
    if (!f.exists() || !f.isDirectory) return "Path must be an existing directory"
    // Optional: basic Gradle/Compose project hint
    val hasGradle = File(f, "gradlew").exists() || File(f, "build.gradle.kts").exists()
    if (!hasGradle) {
        return null // allow any folder; IDE can scaffold later
    }
    return null
}