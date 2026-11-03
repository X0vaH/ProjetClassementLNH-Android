package cstjean.mobile2.classementlnh.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cstjean.mobile2.classementlnh.repository.ClassementRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ClassementViewModel(
    private val repository: ClassementRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClassementUiState())
    val uiState: StateFlow<ClassementUiState> = _uiState.asStateFlow()

    init {
        observerDonneesLocales()
    }

    private fun observerDonneesLocales() {
        viewModelScope.launch {
            repository.observerClassement().collect { liste ->
                _uiState.update { it.copy(equipes = liste) }
            }
        }
        viewModelScope.launch {
            repository.observerDerniereSynchronisation().collect { dateIso ->
                _uiState.update { it.copy(dateDerniereSync = dateIso) }
            }
        }
    }

    fun synchroniser() {
        viewModelScope.launch {
            _uiState.update { it.copy(estEnChargement = true, messageErreur = null) }
            try {
                repository.synchroniser()
                _uiState.update { it.copy(estEnChargement = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        estEnChargement = false,
                        messageErreur = "Erreur de synchronisation : ${e.localizedMessage ?: "Réseau indisponible"}"
                    )
                }
            }
        }
    }
}
