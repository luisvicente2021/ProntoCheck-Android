package com.example.prontocheck.data.network

import com.example.prontocheck.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = BuildConfig.SUPABASE_URL
    private const val API_KEY = BuildConfig.SUPABASE_KEY

    // Token del usuario que inició sesión
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    private val client: OkHttpClient by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->

                val original = chain.request()

                val requestBuilder = original.newBuilder()
                    .header("apikey", API_KEY)
                    .header("Content-Type", "application/json")
                    .header("Prefer", "minimal")

                // Si existe una sesión, usamos el token del usuario.
                // Si todavía no ha iniciado sesión, usamos la API key.
                val token = accessToken ?: API_KEY

                android.util.Log.d(
                    "AUTH_DEBUG",
                    "Retrofit usa sesión autenticada: ${!accessToken.isNullOrBlank()}"
                )

                requestBuilder.header(
                    "Authorization",
                    "Bearer $token"
                )

                requestBuilder.header(
                    "Authorization",
                    "Bearer $token"
                )

                requestBuilder.method(
                    original.method,
                    original.body
                )

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    val instance: SupabaseApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SupabaseApi::class.java)
    }
}