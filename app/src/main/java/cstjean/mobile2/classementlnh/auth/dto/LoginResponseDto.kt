package cstjean.mobile2.classementlnh.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponseDto(
    val accessToken: String
)
