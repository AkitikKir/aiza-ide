package com.aiza.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TerminalView(output: String) {
    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(8.dp)) {
        Text("Terminal", color = Color.Green, style = MaterialTheme.typography.caption)
        Divider(color = Color.DarkGray)
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Text(text = output, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
