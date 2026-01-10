package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class ShellTool : Tool {
    override val name: String = "shell"
    override val description: String = "Execute shell commands with safety controls"

    // JSON Schema describing parameters
    override val schema: JsonElement = buildJsonObject {
        put("type", JsonPrimitive("object"))
        put(
            "properties",
            buildJsonObject {
                put(
                    "command",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Shell command to execute"))
                    }
                )
                put(
                    "workingDirectory",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Working directory for the command"))
                    }
                )
                put(
                    "timeout",
                    buildJsonObject {
                        put("type", JsonPrimitive("number"))
                        put("description", JsonPrimitive("Timeout in seconds (default: 30)"))
                    }
                )
            }
        )
        put(
            "required",
            buildJsonArray {
                add(JsonPrimitive("command"))
            }
        )
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
        val command = request.parameters["command"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: command")

        // Check for dangerous commands
        if (isDangerousCommand(command)) {
            return ErrorResult(request.requestId, "Dangerous command blocked: $command")
        }

        // Check if command requires approval
        if (!isSafeCommand(command) && !request.dryRun) {
            return ApprovalRequiredResult(
                request.requestId,
                "Command requires approval: $command",
                buildJsonObject {
                    put("command", JsonPrimitive(command))
                    put("workingDirectory", JsonPrimitive(request.parameters["workingDirectory"]?.jsonPrimitive?.content ?: "."))
                    put("type", JsonPrimitive("shell"))
                }
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
        val workingDir = request.parameters["workingDirectory"]?.jsonPrimitive?.content ?: "."
        val timeout = request.parameters["timeout"]?.jsonPrimitive?.content?.toLongOrNull() ?: 30L

        return try {
            val process = ProcessBuilder("sh", "-c", command)
                .directory(File(workingDir))
                .redirectErrorStream(true)
                .start()

            // Wait for process with timeout
            val exited = process.waitFor(timeout, java.util.concurrent.TimeUnit.SECONDS)

            if (!exited) {
                process.destroy()
                return ErrorResult(
                    request.requestId,
                    "Command timed out after ${timeout}s: $command",
                    buildJsonObject {
                        put("command", JsonPrimitive(command))
                        put("workingDirectory", JsonPrimitive(workingDir))
                    }
                )
            }

            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.exitValue()

            if (exitCode == 0) {
                SuccessResult(
                    request.requestId,
                    "Command executed successfully",
                    buildJsonObject {
                        put("command", JsonPrimitive(command))
                        put("output", JsonPrimitive(output))
                        put("exitCode", JsonPrimitive(exitCode))
                        put("workingDirectory", JsonPrimitive(workingDir))
                    }
                )
            } else {
                ErrorResult(
                    request.requestId,
                    "Command failed with exit code $exitCode",
                    buildJsonObject {
                        put("command", JsonPrimitive(command))
                        put("output", JsonPrimitive(output))
                        put("exitCode", JsonPrimitive(exitCode))
                        put("workingDirectory", JsonPrimitive(workingDir))
                    }
                )
            }
        } catch (e: Exception) {
            ErrorResult(
                request.requestId,
                "Failed to execute command: ${e.message}",
                buildJsonObject {
                    put("command", JsonPrimitive(command))
                    put("workingDirectory", JsonPrimitive(workingDir))
                }
            )
        }
    }
}