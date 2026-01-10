package com.aiza.agent.tools

import com.aiza.agent.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.File

class FileTool : Tool {
    override val name: String = "file"
    override val description: String = "Create, read, update, and delete files"
    
    override val schema: JsonObject = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("operation", buildJsonObject {
                put("type", "string")
                put("enum", listOf("create", "read", "update", "delete", "list"))
                put("description", "File operation to perform")
            })
            put("path", buildJsonObject {
                put("type", "string")
                put("description", "Path to the file or directory")
            })
            put("content", buildJsonObject {
                put("type", "string")
                put("description", "Content for create/update operations")
            })
        })
        put("required", Json.encodeToJsonElement(listOf("operation", "path")))
    }

    override suspend fun execute(request: ToolRequest): ToolResult {
        val operation = request.parameters["operation"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: operation"
        )
        val path = request.parameters["path"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: path"
        )

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
            return SuccessResult(request.requestId, "Dry run: Would create file at $path")
        }

        val content = request.parameters["content"] as? String ?: ""
        val file = File(path)
        
        return try {
            file.parentFile?.mkdirs()
            file.writeText(content)
            SuccessResult(request.requestId, "Created file: $path", mapOf("path" to path))
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
            SuccessResult(request.requestId, "Read file: $path", mapOf(
                "path" to path,
                "content" to content,
                "size" to content.length
            ))
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to read file: ${e.message}")
        }
    }

    private fun updateFile(request: ToolRequest, path: String): ToolResult {
        if (request.dryRun) {
            return SuccessResult(request.requestId, "Dry run: Would update file at $path")
        }

        val content = request.parameters["content"] as? String ?: return ErrorResult(
            request.requestId, "Missing required parameter: content for update operation"
        )
        val file = File(path)
        
        return try {
            if (!file.exists()) {
                return ErrorResult(request.requestId, "File not found: $path")
            }
            file.writeText(content)
            SuccessResult(request.requestId, "Updated file: $path", mapOf("path" to path))
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to update file: ${e.message}")
        }
    }

    private fun deleteFile(request: ToolRequest, path: String): ToolResult {
        if (request.dryRun) {
            return SuccessResult(request.requestId, "Dry run: Would delete file at $path")
        }

        val file = File(path)
        
        return try {
            if (!file.exists()) {
                return ErrorResult(request.requestId, "File not found: $path")
            }
            if (file.delete()) {
                SuccessResult(request.requestId, "Deleted file: $path", mapOf("path" to path))
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
            val files = dir.listFiles()?.map { file ->
                mapOf(
                    "name" to file.name,
                    "path" to file.path,
                    "isDirectory" to file.isDirectory,
                    "size" to file.length(),
                    "lastModified" to file.lastModified()
                )
            } ?: emptyList()
            
            SuccessResult(request.requestId, "Listed files in: $path", mapOf("files" to files))
        } catch (e: Exception) {
            ErrorResult(request.requestId, "Failed to list files: ${e.message}")
        }
    }
}