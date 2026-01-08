# Aiza API Queries Example

This file contains examples of how to interact with the Aiza API.
**Note: The original file with a real key is attached separately. DO NOT COMMIT REAL KEYS.**

## 1. Chat Completion (curl)

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

## 2. List Models (curl)

```bash
curl -X GET 'https://api.aiza-ai.ru/v1/models' -H "Authorization: Bearer ${AIZA_API_KEY}"
```

## 3. Kotlin (Ktor) Example

```kotlin
val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
}

val response: HttpResponse = client.post("https://api.aiza-ai.ru/v1/chat/completions") {
    header(HttpHeaders.Authorization, "Bearer ${System.getenv("AIZA_API_KEY")}")
    contentType(ContentType.Application.Json)
    setBody(ChatRequest(
        model = "groq/compound",
        messages = listOf(ChatMessage("user", "Hello"))
    ))
}
```
