package cstjean.mobile2.classementlnh.database.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassementDao {
    /** Upsert : met à jour les équipes existantes (clé = abbrev) sans créer de doublons. */
    @Upsert
    suspend fun enregistrer(equipes: List<EquipeClassementEntity>)

    @Query("SELECT * FROM classement ORDER BY points DESC, rangConference ASC")
    fun observerClassement(): Flow<List<EquipeClassementEntity>>

    /** Lecture ponctuelle de toutes les équipes (utilisée par le service d'exportation). */
    @Query("SELECT * FROM classement ORDER BY points DESC, rangConference ASC")
    suspend fun obtenirToutes(): List<EquipeClassementEntity>
}
