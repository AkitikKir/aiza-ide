package com.aiza.core

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class AizaApiClient(
    private val apiKey: String,
    private val baseUrl: String = "https://api.aiza-ai.ru/v1"
) {
    private val client = HttpClient(CIO) {
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
