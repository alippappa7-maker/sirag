package com.siraj.app.core.network

interface NetworkClient {
    suspend fun <T> get(url: String): T
    suspend fun <T> post(url: String, body: Any): T
}

// Will be implemented later with Ktor or Retrofit
class SirajNetworkClient : NetworkClient {
    override suspend fun <T> get(url: String): T {
        TODO("Not yet implemented")
    }

    override suspend fun <T> post(url: String, body: Any): T {
        TODO("Not yet implemented")
    }
}
