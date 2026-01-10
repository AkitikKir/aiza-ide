package com.aiza.agent

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Base interface for all tools that can be executed by the AI agent
 */
interface Tool {
    val name: String
    val description: String
    val schema: JsonElement

    suspend fun execute(request: ToolRequest): ToolResult
}

/**
 * Request for tool execution
 */
@Serializable
data class ToolRequest(
    val tool: String,
    val parameters: Map<String, JsonElement>,
    val requestId: String = "",
    val dryRun: Boolean = false
)

/**
 * Result of tool execution
 */
@Serializable
sealed interface ToolResult {
    val requestId: String
    val success: Boolean
}

@Serializable
data class SuccessResult(
    override val requestId: String,
    val output: String,
    val data: JsonElement? = null
) : ToolResult {
    override val success: Boolean = true
}

@Serializable
data class ErrorResult(
    override val requestId: String,
    val error: String,
    val details: JsonElement? = null
) : ToolResult {
    override val success: Boolean = false
}

@Serializable
data class ApprovalRequiredResult(
    override val requestId: String,
    val message: String,
    val details: JsonElement? = null
) : ToolResult {
    override val success: Boolean = false
}