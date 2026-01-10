package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

class ShellTool : Tool {
    override val name: String = "shell"
    override val description: String = "Execute shell commands with safety controls"
    
    override val schema: JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("command", buildJsonObject {
                put("type", "string")
                put("description", "Shell command to execute")
            })
            put("workingDirectory", buildJsonObject {
                put("type", "string")
                put("description", "Working directory for the command")
            })
            put("timeout", buildJsonObject {
                put("type", "number")
                put("description", "Timeout in seconds (default: 30)")
            })
        })
        put("required", Json.encodeToJsonElement(listOf("command")))
    }

    // Allowlist of safe commands that don't require approval
    private val safeCommands = setOf(
        "ls", "pwd", "cd", "cat", "echo", "find", "grep", "head", "tail",
        "wc", "mkdir", "touch", "cp", "mv", "rm", "chmod", "chown"
    )

    // Blocklist of dangerous commands
    private val dangerousCommands = setOf(
        "rm -rf /", "rm -rf /*", "rm -rf .", "rm -rf *",
        ":(){ :|:& };:", "mkfs", "dd if=/dev/random", "shutdown", "reboot",
        "halt", "poweroff", "init 0", "init 6"
    )

    override suspend fun execute(request: ToolRequest): ToolResult {
        val command = request.parameters["command"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: command"
        )

        // Check for dangerous commands
        if (isDangerousCommand(command)) {
            return ErrorResult(request.requestId, "Dangerous command blocked: $command")
        }

        // Check if command requires approval
        if (!isSafeCommand(command) && !request.dryRun) {
            return ApprovalRequiredResult(
                request.requestId,
                "Command requires approval: $command",
                mapOf(
                    "command" to command,
                    "workingDirectory" to (request.parameters["workingDirectory"] as? String ?: "."),
                    "type" to "shell"
                )
            )
        }

        if (request.dryRun) {
            return SuccessResult(request.requestId, "Dry run: Would execute: $command")
        }

        return executeCommand(request, command)
    }

    private fun isDangerousCommand(command: String): Boolean {
        return dangerousCommands.any { dangerous ->
            command.contains(dangerous, ignoreCase = true)
        }
    }

    private fun isSafeCommand(command: String): Boolean {
        val firstWord = command.trim().split(" ").first()
        return safeCommands.contains(firstWord) && 
               !command.contains("&&") && 
               !command.contains("||") && 
               !command.contains(";") &&
               !command.contains("|") &&
               !command.contains(">") &&
               !command.contains("<") &&
               !command.contains("`")
    }

    private fun executeCommand(request: ToolRequest, command: String): ToolResult {
        val workingDir = request.parameters["workingDirectory"] as? String ?: "."
        val timeout = (request.parameters["timeout"] as? Number)?.toLong() ?: 30L

        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .directory(File(workingDir))
                .redirectErrorStream(true)
                .start()

            // Wait for process with timeout
            val exited = process.waitFor(timeout, java.util.concurrent.TimeUnit.SECONDS)
            
            if (!exited) {
                process.destroy()
                return ErrorResult(request.requestId, "Command timed out after ${timeout}s: $command")
            }

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.exitValue()

            if (exitCode == 0) {
                SuccessResult(request.requestId, "Command executed successfully", mapOf(
                    "command" to command,
                    "output" to output,
                    "exitCode" to exitCode,
                    "workingDirectory" to workingDir
                ))
            } else {
                ErrorResult(request.requestId, "Command failed with exit code $exitCode", mapOf(
                    "command" to command,
                    "output" to output,
                    "exitCode" to exitCode,
                    "workingDirectory" to workingDir
                ))
            }
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to execute command: ${e.message}")
        }
    }
}