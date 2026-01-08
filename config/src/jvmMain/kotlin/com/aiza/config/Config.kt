package com.aiza.config

import io.github.cdimascio.dotenv.Dotenv

object Config {
    private val dotenv = Dotenv.configure()
        .ignoreIfMissing()
        .load()

    val AIZA_API_KEY: String = dotenv.get("AIZA_API_KEY") ?: ""
    val AIZA_BASE_URL: String = dotenv.get("AIZA_BASE_URL") ?: "https://api.aiza-ai.ru/v1"
}
