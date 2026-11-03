package cstjean.mobile2.classementlnh.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface LnhApiService {
    @GET("standings/now")
    suspend fun obtenirClassement(): ReponseClassementDto
}

object RetrofitInstance {
    private const val BASE_URL = "https://api-web.nhle.com/v1/"

    val api: LnhApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LnhApiService::class.java)
    }
}
