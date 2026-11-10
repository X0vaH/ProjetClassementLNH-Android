package cstjean.mobile2.classementlnh.auth.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(
    val username: String,
    val password: String
)
