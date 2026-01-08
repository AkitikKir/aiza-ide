package com.aiza.agent

import com.aiza.core.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

@Serializable
data class AgentCommand(
    val type: String,
    val path: String? = null,
    val content: String? = null,
    val command: String? = null,
    val patch: String? = null
)

class ChatAgent(private val apiClient: AizaApiClient) {
    private val _history = MutableStateFlow<List<Message>>(emptyList())
    val history = _history.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendMessage(content: String, model: String = "groq/compound"): String {
        val userMessage = Message("user", content)
        _history.value += userMessage

        val request = ChatRequest(
            model = model,
            messages = _history.value
        )

        val response = apiClient.getChatCompletion(request)
        val assistantMessage = response.choices.first().message
        _history.value += assistantMessage

        return assistantMessage.content
    }

    fun parseCommands(content: String): List<AgentCommand> {
        val commands = mutableListOf<AgentCommand>()
        val regex = "```json\\s*(\\{[\\s\\S]*?\\})\\s*```".toRegex()
        regex.findAll(content).forEach { match ->
            try {
                val cmd = json.decodeFromString<AgentCommand>(match.groupValues[1])
                commands.add(cmd)
            } catch (e: Exception) {
                println("Failed to parse command: ${e.message}")
            }
        }
        return commands
    }

    fun executeCommand(command: AgentCommand): String {
        return when (command.type) {
            "file.create" -> createFile(command.path!!, command.content!!)
            "file.edit" -> editFile(command.path!!, command.content!!)
            "shell.run" -> runShell(command.command!!)
            else -> "Unknown command type: ${command.type}"
        }
    }

    private fun createFile(path: String, content: String): String {
        return try {
            val file = File(path)
            file.parentFile?.mkdirs()
            file.writeText(content)
            "File created successfully: $path"
        } catch (e: Exception) {
            "Error creating file: ${e.message}"
        }
    }

    private fun editFile(path: String, content: String): String {
        return try {
            val file = File(path)
            if (!file.exists()) return "File does not exist: $path"
            file.writeText(content)
            "File updated successfully: $path"
        } catch (e: Exception) {
            "Error updating file: ${e.message}"
        }
    }

    private fun runShell(command: String): String {
        return try {
            val process = Runtime.getRuntime().exec(command)
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            if (error.isNotEmpty()) "Output: $output\nError: $error" else output
        } catch (e: Exception) {
            "Error running shell: ${e.message}"
        }
    }

    fun clearHistory() {
        _history.value = emptyList()
    }
}
