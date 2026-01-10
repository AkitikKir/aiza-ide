package com.aiza.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileExplorerView(
    rootPath: String,
    onFileSelected: (File) -> Unit
) {
    val rootFile = remember(rootPath) { File(rootPath).absoluteFile }
    var refreshTick by remember { mutableStateOf(0) }

    // UI state
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var renameTarget by remember { mutableStateOf<File?>(null) }
    var deleteTarget by remember { mutableStateOf<File?>(null) }
    var newItemParent by remember { mutableStateOf<File?>(null) }
    var newItemIsDir by remember { mutableStateOf(false) }
    var newItemName by remember { mutableStateOf("") }
    var renameNewName by remember { mutableStateOf("") }

    // Expand/collapse state per path
    val expanded = remember { mutableStateMapOf<String, Boolean>() }
    // Ensure root expanded by default
    LaunchedEffect(rootFile.path) {
        if (expanded[rootFile.path] == null) expanded[rootFile.path] = true
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "Explorer",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )
            Row {
                TextButton(onClick = { refreshTick++ }) { Text("Refresh") }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = {
                    newItemParent = rootFile
                    newItemIsDir = true
                    newItemName = ""
                }) { Text("New Folder") }
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = {
                    newItemParent = rootFile
                    newItemIsDir = false
                    newItemName = ""
                }) { Text("New File") }
            }
        }
        Divider()

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            key(refreshTick, rootFile.path) {
                FileNodeView(
                    file = rootFile,
                    level = 0,
                    expanded = expanded,
                    selectedPath = selectedPath,
                    onSelect = { selectedPath = it.absolutePath },
                    onOpen = onFileSelected,
                    onRequestNew = { parent, isDir ->
                        newItemParent = parent
                        newItemIsDir = isDir
                        newItemName = ""
                    },
                    onRequestRename = { target ->
                        renameTarget = target
                        renameNewName = target.name
                    },
                    onRequestDelete = { target ->
                        deleteTarget = target
                    }
                )
            }
        }
    }

    // Dialogs

    if (newItemParent != null) {
        AlertDialog(
            onDismissRequest = { newItemParent = null },
            title = { Text(if (newItemIsDir) "Create Folder" else "Create File") },
            text = {
                Column {
                    Text("Parent: ${newItemParent!!.absolutePath}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newItemName,
                        onValueChange = { newItemName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val parent = newItemParent!!
                    val target = File(parent, newItemName)
                    runCatching {
                        if (newItemIsDir) {
                            target.mkdirs()
                        } else {
                            target.parentFile?.mkdirs()
                            target.createNewFile()
                        }
                    }
                    // Expand parent and refresh
                    newItemParent = null
                    refreshTick++
                }) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { newItemParent = null }) { Text("Cancel") }
            }
        )
    }

    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename") },
            text = {
                Column {
                    Text("From: ${renameTarget!!.name}")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = renameNewName,
                        onValueChange = { renameNewName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New name") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val src = renameTarget!!
                    val dst = File(src.parentFile, renameNewName)
                    runCatching { src.renameTo(dst) }
                    if (selectedPath == src.absolutePath) {
                        selectedPath = dst.absolutePath
                    }
                    renameTarget = null
                    refreshTick++
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete") },
            text = {
                Column {
                    Text("Delete '${deleteTarget!!.name}'?")
                    Spacer(Modifier.height(4.dp))
                    Text("This action cannot be undone.", color = Color(0xFFB00020))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val tgt = deleteTarget!!
                    runCatching { tgt.deleteRecursively() }
                    if (selectedPath == tgt.absolutePath) selectedPath = null
                    deleteTarget = null
                    refreshTick++
                }) { Text("Delete", color = Color(0xFFB00020)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun FileNodeView(
    file: File,
    level: Int,
    expanded: MutableMap<String, Boolean>,
    selectedPath: String?,
    onSelect: (File) -> Unit,
    onOpen: (File) -> Unit,
    onRequestNew: (File, Boolean) -> Unit,
    onRequestRename: (File) -> Unit,
    onRequestDelete: (File) -> Unit
) {
    val isDir = file.isDirectory
    val path = file.absolutePath
    val isExpanded = expanded[path] ?: false
    val isSelected = selectedPath == path

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isSelected) Color(0xFFE0F7FA) else Color.Transparent)
            .padding(start = (level * 12).dp, top = 2.dp, bottom = 2.dp)
            .clickable {
                onSelect(file)
                if (!isDir) onOpen(file)
            },
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row {
            if (isDir) {
                Text(
                    text = if (isExpanded) "▼" else "▶",
                    modifier = Modifier
                        .width(18.dp)
                        .clickable {
                            expanded[path] = !isExpanded
                        }
                )
            } else {
                Spacer(Modifier.width(18.dp))
            }
            Text(if (isDir) "📁 ${file.name.ifEmpty { "/" }}" else "📄 ${file.name}")
        }

        // Row actions for quick ops on the selected item
        Row {
            if (isDir) {
                TextButton(onClick = { onRequestNew(file, true) }) { Text("New Folder") }
                TextButton(onClick = { onRequestNew(file, false) }) { Text("New File") }
            }
            TextButton(onClick = { onRequestRename(file) }) { Text("Rename") }
            TextButton(onClick = { onRequestDelete(file) }) { Text("Delete") }
        }
    }

    if (isDir && isExpanded) {
        val children = remember(path) { listChildren(file) }
        Column {
            children.forEach { child ->
                FileNodeView(
                    file = child,
                    level = level + 1,
                    expanded = expanded,
                    selectedPath = selectedPath,
                    onSelect = onSelect,
                    onOpen = onOpen,
                    onRequestNew = onRequestNew,
                    onRequestRename = onRequestRename,
                    onRequestDelete = onRequestDelete
                )
            }
        }
    }
}

private fun listChildren(dir: File): List<File> {
    if (!dir.exists() || !dir.isDirectory) return emptyList()
    return (dir.listFiles()?.toList() ?: emptyList())
        .sortedWith(
            compareBy<File>({ !it.isDirectory }, { it.name.lowercase(Locale.getDefault()) })
        )
}
