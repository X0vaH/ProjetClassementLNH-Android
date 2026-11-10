package cstjean.mobile2.classementlnh.auth

import cstjean.mobile2.classementlnh.auth.dto.LoginRequestDto
import cstjean.mobile2.classementlnh.auth.dto.LoginResponseDto
import cstjean.mobile2.classementlnh.auth.dto.ProfilDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {
    @POST("auth/login")
    suspend fun connexion(@Body requete: LoginRequestDto): LoginResponseDto

    @GET("auth/me")
    suspend fun profil(): ProfilDto
}
