package cstjean.mobile2.classementlnh.viewmodel

import cstjean.mobile2.classementlnh.data.Profil

data class ProfilUiState(
    val profil: Profil? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)
