package cstjean.mobile2.classementlnh.database.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassementDao {
    @Upsert
    suspend fun enregistrer(equipes: List<EquipeClassementEntity>)

    @Query("SELECT * FROM classement ORDER BY points DESC, rangConference ASC")
    fun observerClassement(): Flow<List<EquipeClassementEntity>>

    @Query("SELECT * FROM classement")
    suspend fun obtenirToutes(): List<EquipeClassementEntity>

    @Transaction
    @Query("SELECT * FROM classement")
    fun obtenirTousLesFilmsAvecRealisateurPourExportEtSync(): List<FilmAvecRealisateur>

    @Query("SELECT * FROM classement WHERE LOWER(nom) = LOWER(:nom) LIMIT 1")
    suspend fun getClassementParTitre(titre: String): EquipeClassementEntity?

    @Update
    suspend fun mettreAJourClassement(film: EquipeClassementEntity)
}
