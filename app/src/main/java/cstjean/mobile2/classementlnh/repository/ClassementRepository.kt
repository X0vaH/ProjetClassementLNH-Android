package cstjean.mobile2.classementlnh.repository

import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.data.remote.LnhApiService
import cstjean.mobile2.classementlnh.database.local.ClassementDao
import cstjean.mobile2.classementlnh.database.local.SyncMetadataDao
import cstjean.mobile2.classementlnh.database.local.SyncMetadataEntity
import cstjean.mobile2.classementlnh.database.local.toEntity
import cstjean.mobile2.classementlnh.database.toUiModelList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ClassementRepository(
    private val api: LnhApiService,
    private val dao: ClassementDao,
    private val metadataDao: SyncMetadataDao
) {
    /**
     * Observe le classement et convertit automatiquement les Entities Room
     * en Modèles UI exploitables directement par le ViewModel.
     */
    fun observerClassement(): Flow<List<EquipeUiModel>> {
        return dao.observerClassement().map { entities ->
            entities.toUiModelList()
        }
    }

    /**
     * Observe la dernière synchronisation (on peut retourner directement
     * la chaîne de caractères ISO ou un modèle dédié si besoin).
     */
    fun observerDerniereSynchronisation(): Flow<String?> {
        return metadataDao.observerDerniereSynchronisation().map { metadata ->
            metadata?.dateHeureIso
        }
    }

    /**
     * Effectue l'appel réseau, convertit le DTO en Entity Room,
     * puis sauvegarde le tout en local.
     */
    suspend fun synchroniser() {
        val reponse = api.obtenirClassement()
        val equipes = reponse.standings.map { it.toEntity() }
        dao.enregistrer(equipes)

        val dateIso = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.format(Date())

        metadataDao.enregistrerDerniereSynchronisation(
            SyncMetadataEntity(id = 1, dateHeureIso = dateIso)
        )
    }

    /**
     * Fournit les données converties en modèles UI pour l'exportation
     * (utilisable par un Foreground Service par exemple).
     */
    suspend fun obtenirClassementPourExport(): List<EquipeUiModel> {
        return dao.obtenirToutes().toUiModelList()
    }
}
