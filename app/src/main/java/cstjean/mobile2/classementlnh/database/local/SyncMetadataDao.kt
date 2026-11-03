package cstjean.mobile2.classementlnh.database.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncMetadataDao {
    @Upsert
    suspend fun enregistrerDerniereSynchronisation(metadata: SyncMetadataEntity)

    @Query("SELECT * FROM sync_metadata WHERE id = 1")
    fun observerDerniereSynchronisation(): Flow<SyncMetadataEntity?>

    @Query("SELECT * FROM sync_metadata WHERE id = 1")
    suspend fun obtenirDerniereSynchronisation(): SyncMetadataEntity?
}
