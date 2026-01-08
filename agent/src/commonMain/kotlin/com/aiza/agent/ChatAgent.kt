package com.aiza.agent

import com.aiza.core.*
import kotlinx.serialization.json.*
import java.io.File

class ChatAgent(private val apiClient: AizaApiClient) {
    private val messageHistory = mutableListOf<ChatMessage>()

    suspend fun sendMessage(content: String): String {
        messageHistory.add(ChatMessage("user", content))
        
        val request = ChatRequest(
            model = "groq/compound",
            messages = messageHistory.toList()
        )
        
        val response = apiClient.getChatCompletion(request)
        val assistantMessage = response.choices.firstOrNull()?.message ?: ChatMessage("assistant", "Error: No response")
        
        messageHistory.add(assistantMessage)
        
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
