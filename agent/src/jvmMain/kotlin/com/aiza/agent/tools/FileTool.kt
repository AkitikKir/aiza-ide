package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

class FileTool : Tool {
    override val name: String = "file"
    override val description: String = "Create, read, update, and delete files"

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
                                add(JsonPrimitive("create"))
                                add(JsonPrimitive("read"))
                                add(JsonPrimitive("update"))
                                add(JsonPrimitive("delete"))
                                add(JsonPrimitive("list"))
                            }
                        )
                        put("description", JsonPrimitive("File operation to perform"))
                    }
                )
                put(
                    "path",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Path to the file or directory"))
                    }
                )
                put(
                    "content",
                    buildJsonObject {
                        put("type", JsonPrimitive("string"))
                        put("description", JsonPrimitive("Content for create/update operations"))
                    }
                )
            }
        )
        put(
            "required",
            buildJsonArray {
                add(JsonPrimitive("operation"))
                add(JsonPrimitive("path"))
            }
        )
    }

    override suspend fun execute(request: ToolRequest): ToolResult {
        val operation = request.parameters["operation"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: operation")
        val path = request.parameters["path"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: path")

        return when (operation) {
            "create" -> createFile(request, path)
            "read" -> readFile(request, path)
            "update" -> updateFile(request, path)
            "delete" -> deleteFile(request, path)
            "list" -> listFiles(request, path)
            else -> ErrorResult(request.requestId, "Unknown operation: $operation")
        }
    }

    private fun createFile(request: ToolRequest, path: String): ToolResult {
        if (request.dryRun) {
            return SuccessResult(
                request.requestId,
                "Dry run: Would create file at $path",
                buildJsonObject { put("path", JsonPrimitive(path)) }
            )
        }

        val content = request.parameters["content"]?.jsonPrimitive?.content ?: ""
        val file = File(path)

        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            SuccessResult(
                request.requestId,
                "Created file: $path",
                buildJsonObject { put("path", JsonPrimitive(path)) }
            )
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to create file: ${e.message}")
        }
    }

    private fun readFile(request: ToolRequest, path: String): ToolResult {
        val file = File(path)
        return try {
            if (!file.exists()) {
                return ErrorResult(request.requestId, "File not found: $path")
            }
            val content = file.readText()
            SuccessResult(
                request.requestId,
                "Read file: $path",
                buildJsonObject {
                    put("path", JsonPrimitive(path))
                    put("content", JsonPrimitive(content))
                    put("size", JsonPrimitive(content.length))
                }
            )
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to read file: ${e.message}")
        }
    }

    private fun updateFile(request: ToolRequest, path: String): ToolResult {
        if (request.dryRun) {
            return SuccessResult(
                request.requestId,
                "Dry run: Would update file at $path",
                buildJsonObject { put("path", JsonPrimitive(path)) }
            )
        }

        val content = request.parameters["content"]?.jsonPrimitive?.content
            ?: return ErrorResult(request.requestId, "Missing required parameter: content for update operation")

        val file = File(path)

        return try {
            if (!file.exists()) {
                return ErrorResult(request.requestId, "File not found: $path")
            }
            file.writeText(content)
            SuccessResult(
                request.requestId,
                "Updated file: $path",
                buildJsonObject { put("path", JsonPrimitive(path)) }
            )
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to update file: ${e.message}")
        }
    }

    private fun deleteFile(request: ToolRequest, path: String): ToolResult {
        if (request.dryRun) {
            return SuccessResult(
                request.requestId,
                "Dry run: Would delete file at $path",
                buildJsonObject { put("path", JsonPrimitive(path)) }
            )
        }

        val file = File(path)

        return try {
            if (!file.exists()) {
                return ErrorResult(request.requestId, "File not found: $path")
            }
            if (file.delete()) {
                SuccessResult(
                    request.requestId,
                    "Deleted file: $path",
                    buildJsonObject { put("path", JsonPrimitive(path)) }
                )
            } else {
                ErrorResult(request.requestId, "Failed to delete file: $path")
            }
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to delete file: ${e.message}")
        }
    }

    private fun listFiles(request: ToolRequest, path: String): ToolResult {
        val dir = File(path)

        return try {
            if (!dir.exists() || !dir.isDirectory) {
                return ErrorResult(request.requestId, "Directory not found: $path")
            }
            val filesArray: JsonArray = buildJsonArray {
                dir.listFiles()?.forEach { f ->
                    add(
                        buildJsonObject {
                            put("name", JsonPrimitive(f.name))
                            put("path", JsonPrimitive(f.path))
                            put("isDirectory", JsonPrimitive(f.isDirectory))
                            put("size", JsonPrimitive(f.length()))
                            put("lastModified", JsonPrimitive(f.lastModified()))
                        }
                    )
                }
            }

            SuccessResult(
                request.requestId,
                "Listed files in: $path",
                buildJsonObject { put("files", filesArray) }
            )
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to list files: ${e.message}")
        }
    }
}