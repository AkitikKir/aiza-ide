package com.aiza.agent

import com.aiza.core.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import java.io.File

class ChatAgent(private val apiClient: AizaApiClient) {
    private val _history = MutableStateFlow<List<Message>>(emptyList())
    val history: StateFlow<List<Message>> = _history.asStateFlow()

    suspend fun sendMessage(content: String): String {
        val userMessage = Message("user", content)
        _history.value = _history.value + userMessage
        
        val request = ChatRequest(
            model = "groq/compound",
            messages = _history.value
        )
        
        val response = apiClient.getChatCompletion(request)
        val assistantMessage = response.choices.firstOrNull()?.message ?: Message("assistant", "Error: No response")
        
        _history.value = _history.value + assistantMessage
        
        // Simple command parsing (JSON blocks)
        parseAndExecuteCommands(assistantMessage.content)
        
        return assistantMessage.content
    }

    private fun parseAndExecuteCommands(content: String) {
        val regex = "```json\\s*([\\s\\S]*?)\\s*```".toRegex()
        regex.findAll(content).forEach { match ->
            val jsonString = match.groupValues[1]
            try {
                val json = Json.parseToJsonElement(jsonString).jsonObject
                val command = json["command"]?.jsonPrimitive?.content
                when (command) {
                    "file.create" -> {
                        val path = json["path"]?.jsonPrimitive?.content ?: return@forEach
                        val data = json["content"]?.jsonPrimitive?.content ?: ""
                        createFile(path, data)
                    }
                    "shell.run" -> {
                        val cmd = json["cmd"]?.jsonPrimitive?.content ?: return@forEach
                        runShell(cmd)
                    }
                }
            } catch (e: Exception) {
                println("Failed to parse command: ${e.message}")
            }
        }
    }

    private fun createFile(path: String, content: String) {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content)
        println("Created file: $path")
    }

    private fun runShell(command: String) {
        try {
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().readText()
            println("Shell output for '$command':\n$output")
        } catch (e: Exception) {
            println("Failed to run shell: ${e.message}")
        }
    }
}
