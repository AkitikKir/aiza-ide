package com.aiza.agent

import kotlinx.serialization.json.JsonObject

/**
 * Registry for managing available tools
 */
class ToolRegistry {
    private val tools = mutableMapOf<String, Tool>()
    
    fun register(tool: Tool) {
        tools[tool.name] = tool
    }
    
    fun getTool(name: String): Tool? = tools[name]
    
    fun getAllTools(): List<Tool> = tools.values.toList()
    
    fun getToolSchemas(): Map<String, JsonObject> =
        tools.mapValues { it.value.schema }
    
    suspend fun execute(request: ToolRequest): ToolResult {
        val tool = getTool(request.tool) ?: return ErrorResult(
            request.requestId,
            "Tool '${request.tool}' not found"
        )
        
        return try {
            tool.execute(request)
        } catch (e: Exception) {
            ErrorResult(
                request.requestId,
                "Tool execution failed: ${e.message}",
                mapOf("exception" to e.javaClass.simpleName)
            )
        }
    }
}