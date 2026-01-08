package com.aiza.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun FileExplorerView(rootPath: String, onFileSelected: (File) -> Unit) {
    val files = remember { File(rootPath).listFiles()?.toList() ?: emptyList() }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Explorer", style = MaterialTheme.typography.h6)
        Divider()
        LazyColumn {
            items(files) { file ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFileSelected(file) }
                        .padding(4.dp)
                ) {
                    Text(if (file.isDirectory) "📁 " else "📄 " + file.name)
                }
            }
        }
    }
}
