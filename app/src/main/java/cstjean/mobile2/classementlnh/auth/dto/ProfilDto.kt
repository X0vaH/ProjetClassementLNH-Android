package cstjean.mobile2.classementlnh.auth.dto

import cstjean.mobile2.classementlnh.data.Profil
import kotlinx.serialization.Serializable

@Serializable
data class ProfilDto(
    val firstName: String,
    val lastName: String,
    val email: String
)

fun ProfilDto.toModel() = Profil(prenom = firstName, nom = lastName, courriel = email)
