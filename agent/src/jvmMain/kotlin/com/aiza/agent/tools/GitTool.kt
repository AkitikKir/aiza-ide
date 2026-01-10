package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

class GitTool : Tool {
    override val name: String = "git"
    override val description: String = "Git version control operations"
    
    override val schema: JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("operation", buildJsonObject {
                put("type", "string")
                put("enum", listOf("status", "add", "commit", "push", "pull", "branch", "log", "diff"))
                put("description", "Git operation to perform")
            })
            put("files", buildJsonObject {
                put("type", "array")
                put("items", buildJsonObject { put("type", "string") })
                put("description", "Files to add/commit (for add/commit operations)"
            })
            put("message", buildJsonObject {
                put("type", "string")
                put("description", "Commit message (for commit operation)"
            })
            put("branch", buildJsonObject {
                put("type", "string")
                put("description", "Branch name (for branch/push/pull operations)"
            })
        })
        put("required", Json.encodeToJsonElement(listOf("operation")))
    }

    override suspend fun execute(request: ToolRequest): ToolResult {
        val operation = request.parameters["operation"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: operation"
        )

        if (request.dryRun) {
            return SuccessResult(request.requestId, "Dry run: Would execute git $operation")
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

    private fun gitStatus(request: ToolRequest): ToolResult {
        return executeGitCommand(request, "status --porcelain")
    }

    private fun gitAdd(request: ToolRequest): ToolResult {
        val files = request.parameters["files"] as? List<*> ?: listOf(".")
        val fileArgs = if (files == listOf(".")) "." else files.joinToString(" ")
        return executeGitCommand(request, "add $fileArgs")
    }

    private fun gitCommit(request: ToolRequest): ToolResult {
        val message = request.parameters["message"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: message for commit"
        )
        return executeGitCommand(request, "commit -m \"${message.replace("\"", "\\\"")}\"")
    }

    private fun gitPush(request: ToolRequest): ToolResult {
        val branch = request.parameters["branch"] as? String ?: ""
        val pushTarget = if (branch.isNotEmpty()) "origin $branch" else ""
        return executeGitCommand(request, "push $pushTarget")
    }

    private fun gitPull(request: ToolRequest): ToolResult {
        return executeGitCommand(request, "pull")
    }

    private fun gitBranch(request: ToolRequest): ToolResult {
        return executeGitCommand(request, "branch")
    }

    private fun gitLog(request: ToolRequest): ToolResult {
        return executeGitCommand(request, "log --oneline -10")
    }

    private fun gitDiff(request: ToolRequest): ToolResult {
        return executeGitCommand(request, "diff")
    }

    private fun executeGitCommand(request: ToolRequest, gitCommand: String): ToolResult {
        return try {
            val process = ProcessBuilder("git", *gitCommand.split(" ").toTypedArray())
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (exitCode == 0) {
                SuccessResult(request.requestId, "Git command executed", mapOf(
                    "command" to gitCommand,
                    "output" to output
                ))
            } else {
                ErrorResult(request.requestId, "Git command failed", mapOf(
                    "command" to gitCommand,
                    "output" to output,
                    "exitCode" to exitCode
                ))
            }
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to execute git command: ${e.message}")
        }
    }
}