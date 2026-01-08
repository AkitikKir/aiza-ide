package com.aiza.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun CodeEditorView(content: String, onContentChange: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Text("Editor", style = MaterialTheme.typography.h6)
        Divider()
        TextField(
            value = content,
            onValueChange = onContentChange,
            modifier = Modifier.fillMaxSize(),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colors.surface
            )
        )
    }
}
