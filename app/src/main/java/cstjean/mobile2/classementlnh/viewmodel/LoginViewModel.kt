package cstjean.mobile2.classementlnh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cstjean.mobile2.classementlnh.data.ErreurServeurException
import cstjean.mobile2.classementlnh.data.IdentifiantsInvalidesException
import cstjean.mobile2.classementlnh.data.ReseauIndisponibleException
import cstjean.mobile2.classementlnh.auth.repository.ApiAuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: ApiAuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun connexion(identifiant: String, motDePasse: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.connexion(identifiant, motDePasse)
                _uiState.update { it.copy(isLoading = false, connecte = true) }
            } catch (e: IdentifiantsInvalidesException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Identifiant ou mot de passe incorrect"
                    )
                }
            } catch (e: ReseauIndisponibleException) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Réseau indisponible") }
            } catch (e: ErreurServeurException) {
                // Ajout par rapport à la démo : erreur du serveur
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erreur du serveur") }
            }
        }
    }
}
