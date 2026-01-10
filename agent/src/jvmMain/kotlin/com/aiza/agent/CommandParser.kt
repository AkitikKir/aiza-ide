package com.aiza.agent

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses JSON commands from AI responses and converts them to ToolRequests
 * Expected fenced block example:
 * ```json
 * { "tool": "shell", "command": "echo hi", "timeout": 5 }
 * ```
 */
class CommandParser(private val toolRegistry: ToolRegistry) {

    fun parseCommands(content: String): List<ParsedCommand> {
        val regex = "```json\\s*([\\s\\S]*?)\\s*```".toRegex()
        return regex.findAll(content).mapNotNull { match ->
            parseCommandBlock(match.groupValues[1])
        }.toList()
    }

    private fun parseCommandBlock(jsonString: String): ParsedCommand? {
        return try {
            val root: JsonObject = Json.parseToJsonElement(jsonString).jsonObject
            val toolName = root["tool"]?.jsonPrimitive?.content ?: return null
            // Parameters are the remaining fields as raw JsonElements
            val params: Map<String, JsonElement> = root.filterKeys { it != "tool" }
            ParsedCommand(toolName, params)
        } catch (e: Exception) {
            println("Failed to parse command: ${e.message}")
            null
        }
    }

    data class ParsedCommand(
        val toolName: String,
        val parameters: Map<String, JsonElement>
    )
}