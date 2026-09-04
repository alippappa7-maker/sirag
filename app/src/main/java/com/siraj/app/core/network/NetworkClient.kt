package com.siraj.app.core.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

interface NetworkClient {
    suspend fun <T> get(url: String): T

    suspend fun <T> post(
        url: String,
        body: Any,
    ): T
}

/**
 * Production-ready NetworkClient implementation using OkHttp.
 */
class SirajNetworkClient(
    private val client: OkHttpClient =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build(),
) : NetworkClient {
    private companion object {
        const val TAG = "SirajNetworkClient"
        const val JSON_MEDIA_TYPE = "application/json; charset=utf-8"
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> get(url: String): T =
        withContext(Dispatchers.IO) {
            runCatching {
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .get()
                        .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw NetworkException(response.code, "GET $url failed: ${response.message}")
                    }
                    parseResponse<T>(response.body?.string())
                }
            }.getOrElse { e ->
                Log.e(TAG, "GET $url failed", e)
                throw e
            }
        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T> post(
        url: String,
        body: Any,
    ): T =
        withContext(Dispatchers.IO) {
            runCatching {
                val jsonBody =
                    when (body) {
                        is String -> body
                        is JSONObject -> body.toString()
                        else -> JSONObject(body as? Map<*, *> ?: emptyMap<String, Any>()).toString()
                    }
                val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE.toMediaTypeOrNull())
                val request =
                    Request
                        .Builder()
                        .url(url)
                        .post(requestBody)
                        .build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw NetworkException(response.code, "POST $url failed: ${response.message}")
                    }
                    parseResponse<T>(response.body?.string())
                }
            }.getOrElse { e ->
                Log.e(TAG, "POST $url failed", e)
                throw e
            }
        }

    @Suppress("UNCHECKED_CAST")
    private fun <T> parseResponse(rawBody: String?): T {
        if (rawBody.isNullOrEmpty()) return Unit as T
        // Try to parse as JSON; if it fails, return the raw string
        return runCatching {
            JSONObject(rawBody) as T
        }.recoverCatching {
            rawBody as T
        }.getOrThrow()
    }
}

class NetworkException(
    val statusCode: Int,
    override val message: String,
) : Exception(message)
