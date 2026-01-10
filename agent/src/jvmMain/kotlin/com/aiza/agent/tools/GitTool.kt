package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class GitTool : Tool {
    override val name: String = "git"
    override val description: String = "Git version control operations"

    // JSON Schema describing parameters
    override val schema: JsonElement = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put(
                    "operation",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put(
                            "enum",
                            buildJsonArray {
                                add(JsonPrimitive("status"))
                                add(JsonPrimitive("add"))
                                add(JsonPrimitive("commit"))
                                add(JsonPrimitive("push"))
                                add(JsonPrimitive("pull"))
                                add(JsonPrimitive("branch"))
                                add(JsonPrimitive("log"))
                                add(JsonPrimitive("diff"))
                            }
                        )
                        put("description", JsonPrimitive("Git operation to perform"))
                    }
                )
                put(
                    "files",
                    buildJsonObject {
                        put("type", JsonPrimitive("array"))
                        put(
                            "items",
                            buildJsonObject { put("type", JsonPrimitive("string")) }
                        )
                        put("description", JsonPrimitive("Files to add/commit (for add/commit operations)"))
                    }
                )
                put(
                    "message",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Commit message (for commit operation)"))
                    }
                )
                put(
                    "branch",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Branch name (for branch/push/pull operations)"))
                    }
                )
            }
        )
        put(
            "required",
            buildJsonArray {
                add(JsonPrimitive("operation"))
            }
        )
    }

    override suspend fun execute(request: ToolRequest): ToolResult {
        val operation = request.parameters["operation"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: operation")

        if (request.dryRun) {
            return SuccessResult(
                request.requestId,
                "Dry run: Would execute git $operation",
                buildJsonObject { put("operation", JsonPrimitive(operation)) }
            )
        }

        return when (operation) {
            "status" -> gitStatus(request)
            "add" -> gitAdd(request)
            "commit" -> gitCommit(request)
            "push" -> gitPush(request)
            "pull" -> gitPull(request)
            "branch" -> gitBranch(request)
            "log" -> gitLog(request)
            "diff" -> gitDiff(request)
            else -> ErrorResult(request.requestId, "Unknown git operation: $operation")
        }
    }

    private fun gitStatus(request: ToolRequest): ToolResult =
        executeGitCommand(request, listOf("status", "--porcelain"))

    private fun gitAdd(request: ToolRequest): ToolResult {
        val filesEl = request.parameters["files"]
        val files: List<String> = when (filesEl) {
            is JsonArray -> filesEl.mapNotNull { it.jsonPrimitive.contentOrNull }.ifEmpty { listOf(".") }
            is JsonElement -> listOfNotNull(filesEl.jsonPrimitive.contentOrNull)
            else -> listOf(".")
        }
        val args = mutableListOf("add") + files
        return executeGitCommand(request, args)
    }

    private fun gitCommit(request: ToolRequest): ToolResult {
        val message = request.parameters["message"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: message for commit")
        // Escape double quotes for shell safety if needed; ProcessBuilder avoids shell expansion here.
        return executeGitCommand(request, listOf("commit", "-m", message))
    }

    private fun gitPush(request: ToolRequest): ToolResult {
        val branch = request.parameters["branch"]?.jsonPrimitive?.content.orEmpty()
        val args = if (branch.isNotEmpty()) listOf("push", "origin", branch) else listOf("push")
        return executeGitCommand(request, args)
    }

    private fun gitPull(request: ToolRequest): ToolResult =
        executeGitCommand(request, listOf("pull"))

    private fun gitBranch(request: ToolRequest): ToolResult =
        executeGitCommand(request, listOf("branch"))

    private fun gitLog(request: ToolRequest): ToolResult =
        executeGitCommand(request, listOf("log", "--oneline", "-10"))

    private fun gitDiff(request: ToolRequest): ToolResult =
        executeGitCommand(request, listOf("diff"))

    private fun executeGitCommand(request: ToolRequest, gitArgs: List<String>): ToolResult {
        return try {
            val process = ProcessBuilder(listOf("git") + gitArgs)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                SuccessResult(
                    request.requestId,
                    "Git command executed",
                    buildJsonObject {
                        put("args", JsonPrimitive(gitArgs.joinToString(" ")))
                        put("output", JsonPrimitive(output))
                        put("exitCode", JsonPrimitive(exitCode))
                    }
                )
            } else {
                ErrorResult(
                    request.requestId,
                    "Git command failed",
                    buildJsonObject {
                        put("args", JsonPrimitive(gitArgs.joinToString(" ")))
                        put("output", JsonPrimitive(output))
                        put("exitCode", JsonPrimitive(exitCode))
                    }
                )
            }
        } catch (e: Exception) {
            ErrorResult(
                request.requestId,
                "Failed to execute git command: ${e.message}",
                buildJsonObject {
                    put("args", JsonPrimitive(gitArgs.joinToString(" ")))
                }
            )
        }
    }

    private val String?.contentOrNull: String?
        get() = try {
            this?.let { it }
        } catch (_: Exception) { null }
}