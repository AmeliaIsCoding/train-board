package com.example.trainboard.api

import com.example.trainboard.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class API {
    companion object {
        fun client() = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                    },
                )
            }
            install(HttpCache)
            install(DefaultRequest) {
                header("x-api-key", BuildConfig.API_KEY)
            }
        }
    }
}
