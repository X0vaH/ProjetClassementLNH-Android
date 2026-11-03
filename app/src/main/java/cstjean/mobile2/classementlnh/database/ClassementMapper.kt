package cstjean.mobile2.classementlnh.database

import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.database.local.EquipeClassementEntity

/**
 * Convertit une Entity Room en Modèle UI pour l'affichage dans Compose.
 */
fun EquipeClassementEntity.toUiModel(): EquipeUiModel {
    return EquipeUiModel(
        abbrev = this.abbrev,
        nom = this.nom,
        division = this.division,
        conference = this.conference,
        matchsJoues = this.matchsJoues,
        victoires = this.victoires,
        defaites = this.defaites,
        defaitesProlongation = this.defaitesProlongation,
        points = this.points,
        rangConference = this.rangConference,
        rangDivision = this.rangDivision
    )
}

/**
 * Convertit une liste d'Entities en une liste de Modèles UI.
 */
fun List<EquipeClassementEntity>.toUiModelList(): List<EquipeUiModel> {
    return this.map { it.toUiModel() }
}