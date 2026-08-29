package com.siraj.app.data.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var exception: IOException? = null
        var tryCount = 0
        var backoff = 1000L

        while (tryCount < maxRetries) {
            try {
                response = chain.proceed(request)
                if (response.isSuccessful) return response
                // If not successful, close it to avoid leaking
                response.close()
            } catch (e: IOException) {
                exception = e
            }
            tryCount++
            if (tryCount < maxRetries) {
                try {
                    Thread.sleep(backoff)
                } catch (ignored: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                backoff *= 2
            }
        }
        
        return response ?: throw exception ?: IOException("Unknown network error")
    }
}

object RetrofitClient {
    private const val BASE_URL = "https://api.quran.com/api/v4/"

    private val logging = HttpLoggingInterceptor().apply {
        // Reduced from BODY to BASIC to prevent logging sensitive user data or large payloads
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(RetryInterceptor())
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val quranApi: QuranApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
            .create(QuranApi::class.java)
    }
}
