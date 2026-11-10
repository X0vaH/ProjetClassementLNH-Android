package cstjean.mobile2.classementlnh.auth

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val jeton: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requete = chain.request().newBuilder().apply {
            jeton()?.let {
                addHeader("Authorization", "Bearer $it")
            }
        }.build()

        return chain.proceed(requete)
    }
}
