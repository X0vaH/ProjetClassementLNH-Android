package cstjean.mobile2.classementlnh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cstjean.mobile2.classementlnh.data.ErreurServeurException
import cstjean.mobile2.classementlnh.data.ReseauIndisponibleException
import cstjean.mobile2.classementlnh.data.SessionExpireeException
import cstjean.mobile2.classementlnh.auth.SessionManager
import cstjean.mobile2.classementlnh.auth.repository.ApiAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfilViewModel(
    private val repository: ApiAuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfilUiState())
    val uiState: StateFlow<ProfilUiState> = _uiState.asStateFlow()

    init {
        charger()
    }

    fun charger() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val profil = repository.obtenirProfil()
                _uiState.update { it.copy(profil = profil, isLoading = false) }
            } catch (e: SessionExpireeException) {
                // On ferme la session : la navigation nous ramène à la connexion
                repository.deconnexion()
            } catch (e: ReseauIndisponibleException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Réseau indisponible") }
            } catch (e: ErreurServeurException) {
                // Ajout par rapport à la démo : erreur du serveur
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erreur du serveur") }
            }
        }
    }

    fun deconnexion() = repository.deconnexion()
}
