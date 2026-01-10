package com.aiza.agent

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Parses JSON commands from AI responses and converts them to ToolRequests
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
            val json = Json.parseToJsonElement(jsonString).jsonObject
            val toolName = json["tool"]?.jsonPrimitive?.content
            val parameters = json.filterKeys { it != "tool" }
                .mapValues { (_, value) ->
                    when (value) {
                        is JsonObject -> value.toString()
                        else -> value.jsonPrimitive.content
                    }
                }
            
            if (toolName != null) {
                ParsedCommand(toolName, parameters)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Failed to parse command: ${e.message}")
            null
        }
    }
    
    data class ParsedCommand(
        val toolName: String,
        val parameters: Map<String, Any>
    )
}