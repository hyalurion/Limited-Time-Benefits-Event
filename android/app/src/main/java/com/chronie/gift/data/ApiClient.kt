package com.chronie.gift.data

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json

/**
 * Shared Ktor HTTP client used by the remaining in-app API clients
 * ([ServerMonitorApi] and friends).
 *
 * It used to live next to the quiz screen, but that screen was removed when the
 * timed quiz event was taken offline. The client now lives in `data` so the whole
 * app still shares a single connection pool and one set of LAN timeouts.
 */
object ApiClient {
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            connectTimeout = 10000
            socketTimeout = 30000
        }
    }
}
