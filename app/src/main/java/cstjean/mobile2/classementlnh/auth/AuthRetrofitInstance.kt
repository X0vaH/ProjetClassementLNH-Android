package cstjean.mobile2.classementlnh.auth

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object AuthRetrofitInstance {
    private const val BASE_URL = "https://dummyjson.com/"

    private val json = Json {
        ignoreUnknownKeys = true // Ignore les champs JSON non déclarés dans le DTO
    }

    val authApi by lazy {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor { SessionManager.jetonCourant })
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AuthApiService::class.java)
    }
}
