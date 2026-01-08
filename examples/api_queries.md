# Aiza API Examples

> **Note**: These examples are sanitized. Do not commit your real API key. Use the `.env` file for local development.

## Chat Completions

```bash
curl -X POST 'https://api.aiza-ai.ru/v1/chat/completions' \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer ${AIZA_API_KEY}" \
  -d '{
    "model": "groq/compound",
    "messages": [
      {"role": "user", "content": "Привет! Как дела?"}
    ],
    "temperature": 0.7,
    "max_tokens": 1000
  }'
```

## List Models

```bash
curl -X GET 'https://api.aiza-ai.ru/v1/models' \
  -H "Authorization: Bearer ${AIZA_API_KEY}"
```

## Kotlin (Ktor) Example

```kotlin
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

val response = client.post("https://api.aiza-ai.ru/v1/chat/completions") {
    header("Authorization", "Bearer ${AIZA_API_KEY}")
    contentType(ContentType.Application.Json)
    setBody(ChatRequest(
        model = "groq/compound",
        messages = listOf(Message("user", "Hello!"))
    ))
}
```

---
*Note: The original examples provided by the user may contain real keys. Those should NEVER be committed to the repository.*
