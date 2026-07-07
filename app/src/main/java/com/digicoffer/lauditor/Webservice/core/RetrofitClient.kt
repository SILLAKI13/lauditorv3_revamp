package com.digicoffer.lauditor.Webservice.core

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var retrofit: Retrofit? = null
    private var apiService: ApiService? = null
    private var okHttpClient: OkHttpClient? = null

    @Synchronized
    fun getOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val builder = OkHttpClient.Builder()
                .connectTimeout(NetworkConfig.connectTimeoutSeconds, TimeUnit.SECONDS)
                .readTimeout(NetworkConfig.readTimeoutSeconds, TimeUnit.SECONDS)
                .writeTimeout(NetworkConfig.writeTimeoutSeconds, TimeUnit.SECONDS)

            if (NetworkConfig.isLoggingEnabled) {
                val logging = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                builder.addInterceptor(logging)
            }

            for (interceptor in NetworkConfig.customInterceptors) {
                builder.addInterceptor(interceptor)
            }

            okHttpClient = builder.build()
        }
        return okHttpClient!!
    }

    @Synchronized
    fun getApiService(): ApiService {
        if (retrofit == null) {
            // Retrofit requires a valid base URL format even if we override it using absolute paths on @Url
            val dummyBaseUrl = "http://localhost/"
            retrofit = Retrofit.Builder()
                .baseUrl(dummyBaseUrl)
                .client(getOkHttpClient())
                .build()
            apiService = retrofit!!.create(ApiService::class.java)
        }
        return apiService!!
    }
}
