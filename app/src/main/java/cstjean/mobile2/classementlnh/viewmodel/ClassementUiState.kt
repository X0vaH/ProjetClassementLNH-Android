package cstjean.mobile2.classementlnh.viewmodel

import cstjean.mobile2.classementlnh.business.EquipeUiModel

data class ClassementUiState(
    val equipes: List<EquipeUiModel> = emptyList(),
    val estEnChargement: Boolean = false,
    val dateDerniereSync: String? = null,
    val messageErreur: String? = null
)
