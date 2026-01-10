package com.aiza.core

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class AizaApiClient(
    private val apiKey: String,
    private val baseUrl: String = "https://api.aiza-ai.ru/v1"
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                }
            )
        }
    }

    suspend fun getChatCompletion(request: ChatRequest): ChatResponse {
        return client.post("$baseUrl/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.Accept, "application/json")
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // SSE-like streaming of chat completion chunks (for models that support streaming responses).
    fun getChatCompletionStream(request: ChatRequest): Flow<ChatResponseChunk> {
        val streamRequest = ChatRequestStream(
            model = request.model,
            messages = request.messages,
            temperature = request.temperature,
            max_tokens = request.max_tokens,
            stream = true
        )

        return flow {
            val response: HttpResponse = client.post("$baseUrl/chat/completions") {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                header(HttpHeaders.Accept, "text/event-stream")
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(streamRequest)
            }

            val channel: ByteReadChannel = response.bodyAsChannel()

            try {
                while (!channel.isClosedForRead) {
                    // Some environments require an explicit limit parameter
                    val line = channel.readUTF8Line(8192) ?: break
                    val trimmed = line.trim()
                    if (!trimmed.startsWith("data:")) continue

                    val data = trimmed.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isEmpty()) continue

                    runCatching {
                        val chunk = Json.decodeFromString(ChatResponseChunk.serializer(), data)
                        emit(chunk)
                    }.onFailure {
                        // Skip malformed/partial chunks silently to keep stream resilient
                    }
                }
            } finally {
                channel.cancel(cause = null)
            }
        }
    }

    suspend fun getModels(): ModelListResponse {
        return client.get("$baseUrl/models") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            header(HttpHeaders.Accept, "application/json")
        }.body()
    }
}
