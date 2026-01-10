package com.aiza.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ChatRequestStream(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    val max_tokens: Int = 1000,
    val stream: Boolean = true
)

@Serializable
data class ChatResponseChunk(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChatChoiceChunk>
)

@Serializable
data class ChatChoiceChunk(
    val index: Int,
    val delta: MessageDelta,
    val finish_reason: String?
)

@Serializable
data class MessageDelta(
    val role: String? = null,
    val content: String? = null
)

@Serializable
data class StreamError(
    val error: String,
    val code: Int
)