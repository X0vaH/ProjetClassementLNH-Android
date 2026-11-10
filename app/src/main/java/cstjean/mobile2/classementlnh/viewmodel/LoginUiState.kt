package cstjean.mobile2.classementlnh.viewmodel

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val connecte: Boolean = false
)
