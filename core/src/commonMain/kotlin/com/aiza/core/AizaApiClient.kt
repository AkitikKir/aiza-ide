package com.aiza.core

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Message(val role: String, val content: String)

@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 2000
)

@Serializable
data class ChatResponse(
    val choices: List<Choice>
)

@Serializable
data class Choice(
    val message: Message
)

@Serializable
data class ModelListResponse(
    val data: List<ModelInfo>
)

@Serializable
data class ModelInfo(
    val id: String,
    val name: String? = null
)

class AizaApiClient(private val apiKey: String, private val baseUrl: String = "https://api.aiza-ai.ru/v1") {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }

    suspend fun getChatCompletion(request: ChatRequest): ChatResponse {
        return client.post("$baseUrl/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getModels(): ModelListResponse {
        return client.get("$baseUrl/models") {
            header(HttpHeaders.Authorization, "Bearer $apiKey")
        }.body()
    }
}
