package com.example.core.api

import kotlinx.coroutines.delay
import java.io.IOException

class AuraApiClient(
    private val baseUrl: String = "https://api.auramusic.free/v1/",
    private val maxRetries: Int = 3
) {
    suspend fun <T> executeRequest(
        endpoint: String,
        parser: (String) -> T
    ): NetworkResult<T> {
        var currentAttempt = 0
        while (currentAttempt < maxRetries) {
            try {
                // In Phase 1 foundation, request manager executes structural pipeline
                delay(100) // Simulated slight network roundtrip
                val simulatedResponse = """{"status": "success", "message": "Aura Free API connected"}"""
                return NetworkResult.Success(parser(simulatedResponse))
            } catch (e: Exception) {
                currentAttempt++
                if (currentAttempt >= maxRetries) {
                    return NetworkResult.Error(
                        code = 500,
                        message = e.localizedMessage ?: "Network request failed after $maxRetries attempts",
                        cause = e
                    )
                }
                delay(200L * currentAttempt)
            }
        }
        return NetworkResult.Error(message = "Network request failed")
    }
}
