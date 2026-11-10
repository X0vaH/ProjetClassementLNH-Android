package cstjean.mobile2.classementlnh.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _jeton = MutableStateFlow<String?>(null)
    val jeton: StateFlow<String?> = _jeton.asStateFlow()

    val jetonCourant: String? get() = _jeton.value

    fun ouvrirSession(jeton: String) {
        _jeton.value = jeton
    }

    fun fermerSession() {
        _jeton.value = null
    }
}
