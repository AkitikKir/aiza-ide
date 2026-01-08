package com.aiza.config

import io.github.cdimascio.dotenv.Dotenv

object Config {
    private val dotenv = Dotenv.configure()
        .directory("./")
        .ignoreIfMalformed()
        .ignoreIfMissing()
        .load()

    val AIZA_API_KEY: String = System.getenv("AIZA_API_KEY") ?: dotenv.get("AIZA_API_KEY") ?: ""
    val AIZA_BASE_URL: String = System.getenv("AIZA_BASE_URL") ?: dotenv.get("AIZA_BASE_URL") ?: "https://api.aiza-ai.ru/v1"

    fun isConfigured() = AIZA_API_KEY.isNotEmpty()
}
