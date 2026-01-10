package com.aiza.agent

import com.aiza.core.*
import com.aiza.agent.tools.FileTool
import com.aiza.agent.tools.ShellTool
import com.aiza.agent.tools.GitTool
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.*
import java.util.UUID

/**
 * Enhanced chat agent with tool registry and command parsing
 */
class EnhancedChatAgent(private val apiClient: AizaApiClient) {
    private val _history = MutableStateFlow<List<Message>>(emptyList())
    val history: StateFlow<List<Message>> = _history.asStateFlow()

    private val _terminalOutput = MutableStateFlow("")
    val terminalOutput: StateFlow<String> = _terminalOutput.asStateFlow()
    
    private val toolRegistry = ToolRegistry().apply {
        register(FileTool())
        register(ShellTool())
        register(GitTool())
    }
    
    private val commandParser = CommandParser(toolRegistry)
    private val pendingApprovals = mutableMapOf<String, ToolRequest>()

    suspend fun sendMessage(content: String): String {
        val userMessage = Message("user", content)
        _history.update { it + userMessage }
        
        val request = ChatRequest(
            model = "groq/compound",
            messages = _history.value
        )
        
        val response = apiClient.getChatCompletion(request)
        val assistantMessage = response.choices.firstOrNull()?.message ?: Message("assistant", "Error: No response")
        
        _history.update { it + assistantMessage }
        
        // Parse and handle commands
        val commands = commandParser.parseCommands(assistantMessage.content)
        if (commands.isNotEmpty()) {
            handleCommands(commands)
        }
        
        return assistantMessage.content
    }

    suspend fun sendMessageStreaming(content: String) {
        val userMessage = Message("user", content)
        _history.update { it + userMessage }

        val request = ChatRequest(
            model = "groq/compound",
            messages = _history.value
        )

        // Add placeholder assistant message and remember its index
        var assistantIndex = -1
        _history.update {
            assistantIndex = it.size
            it + Message("assistant", "")
        }

        val buffer = StringBuilder()

        // Collect streaming chunks and update assistant message incrementally
        apiClient.getChatCompletionStream(request).collect { chunk ->
            val delta = chunk.choices.firstOrNull()?.delta?.content.orElse("")
            if (delta.isNotEmpty() && assistantIndex >= 0) {
                buffer.append(delta)
                val idx = assistantIndex
                _history.update { list ->
                    val mutable = list.toMutableList()
                    val current = mutable[idx]
                    mutable[idx] = current.copy(content = current.content + delta)
                    mutable
                }
            }
        }

        // After stream completes, parse and handle any commands found in the final assistant content
        val finalContent = buffer.toString()
        if (finalContent.isNotEmpty()) {
            val commands = commandParser.parseCommands(finalContent)
            if (commands.isNotEmpty()) {
                handleCommands(commands)
            }
        }
    }

    // Small helper to avoid null checks noise
    private fun String?.orElse(fallback: String) = this ?: fallback

    // Extract a string field from a JsonElement object safely
    private fun jsonGetString(el: JsonElement?, key: String): String {
        return try {
            (el as? JsonObject)?.get(key)?.jsonPrimitive?.content ?: ""
        } catch (_: Exception) {
            ""
        }
    }
    
    private suspend fun handleCommands(commands: List<CommandParser.ParsedCommand>) {
        for (command in commands) {
            val requestId = UUID.randomUUID().toString()
            val toolRequest = ToolRequest(
                tool = command.toolName,
                parameters = command.parameters,
                requestId = requestId
            )
            
            val result = toolRegistry.execute(toolRequest)
            
            when (result) {
                is ApprovalRequiredResult -> {
                    pendingApprovals[requestId] = toolRequest
                    // Add approval request to chat history
                    _history.update { it + Message("system", 
                        "🔒 Approval required for: ${result.message}\n\n" +
                        "Type '/approve $requestId' to execute or '/deny $requestId' to cancel."
                    )}
                }
                is SuccessResult -> {
                    _history.update { it + Message("system",
                        "✅ ${result.output}"
                    )}
                    if (command.toolName == "shell") {
                        val out = jsonGetString(result.data, "output")
                        if (out.isNotEmpty()) {
                            _terminalOutput.update { it + out + "\n" }
                        }
                    }
                }
                is ErrorResult -> {
                    _history.update { it + Message("system",
                        "❌ ${result.error}"
                    )}
                    if (command.toolName == "shell") {
                        val out = jsonGetString(result.details, "output")
                        if (out.isNotEmpty()) {
                            _terminalOutput.update { it + out + "\n" }
                        }
                    }
                }
            }
        }
    }
    
    suspend fun approveCommand(requestId: String): Boolean {
        val request = pendingApprovals[requestId] ?: return false
        pendingApprovals.remove(requestId)
        
        val result = toolRegistry.execute(request.copy(dryRun = false))
        
        when (result) {
            is SuccessResult -> {
                _history.update { it + Message("system",
                    "✅ Approved and executed: ${result.output}"
                )}
                if (request.tool == "shell") {
                    val out = jsonGetString(result.data, "output")
                    if (out.isNotEmpty()) {
                        _terminalOutput.update { it + out + "\n" }
                    }
                }
                return true
            }
            is ErrorResult -> {
                _history.update { it + Message("system",
                    "❌ Execution failed: ${result.error}"
                )}
                if (request.tool == "shell") {
                    val out = jsonGetString(result.details, "output")
                    if (out.isNotEmpty()) {
                        _terminalOutput.update { it + out + "\n" }
                    }
                }
                return false
            }
            else -> return false
        }
    }
    
    fun denyCommand(requestId: String): Boolean {
        if (pendingApprovals.remove(requestId) != null) {
            _history.update { it + Message("system", 
                "❌ Command denied: $requestId"
            )}
            return true
        }
        return false
    }
    
    fun getPendingApprovals(): Map<String, ToolRequest> = pendingApprovals.toMap()

    suspend fun requestShell(
        command: String,
        workingDirectory: String = ".",
        timeoutSec: Long = 30,
        dryRun: Boolean = false
    ) {
        val requestId = java.util.UUID.randomUUID().toString()
        val toolRequest = ToolRequest(
            tool = "shell",
            parameters = mapOf(
                "command" to JsonPrimitive(command),
                "workingDirectory" to JsonPrimitive(workingDirectory),
                "timeout" to JsonPrimitive(timeoutSec)
            ),
            requestId = requestId,
            dryRun = dryRun
        )

        when (val result = toolRegistry.execute(toolRequest)) {
            is ApprovalRequiredResult -> {
                pendingApprovals[requestId] = toolRequest
                _history.update { it + Message("system",
                    "🔒 Approval required for: Command requires approval: $command\n\n" +
                    "Type '/approve $requestId' to execute or '/deny $requestId' to cancel."
                )}
            }
            is SuccessResult -> {
                _history.update { it + Message("system", "✅ ${result.output}") }
                val out = jsonGetString(result.data, "output")
                if (out.isNotEmpty()) {
                    _terminalOutput.update { it + out + "\n" }
                }
            }
            is ErrorResult -> {
                _history.update { it + Message("system", "❌ ${result.error}") }
                val out = jsonGetString(result.details, "output")
                if (out.isNotEmpty()) {
                    _terminalOutput.update { it + out + "\n" }
                }
            }
        }
    }
}