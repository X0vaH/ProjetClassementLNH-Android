package cstjean.mobile2.classementlnh.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker qui récupère le classement distant (API LNH) et met à jour Room.
 */
class SyncClassementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Début de la synchronisation du classement LNH (tentative ${runAttemptCount + 1})")

        val repository = (applicationContext as ClassementLnhApplication).repository

        try {
            repository.synchroniser()
            Log.d(TAG, "Synchronisation réussie.")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la synchronisation", e)
            // Échec temporaire (ex. : API inaccessible, perte de réseau) : on retente
            if (runAttemptCount < MAX_TENTATIVES) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }

    companion object {
        private const val TAG = "SyncClassementWorker"
        private const val MAX_TENTATIVES = 3
        const val NOM_TRAVAIL_UNIQUE = "SyncClassementUnique"

        /**
         * Planifie la synchronisation périodique (toutes les 4 heures) avec une
         * contrainte de connexion réseau. Le travail unique + KEEP évite de
         * planifier plusieurs synchronisations identiques.
         */
        fun planifier(context: Context) {
            val contraintes = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val requete = PeriodicWorkRequestBuilder<SyncClassementWorker>(4, TimeUnit.HOURS)
                .setConstraints(contraintes)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOM_TRAVAIL_UNIQUE,
                ExistingPeriodicWorkPolicy.KEEP,
                requete
            )
        }
    }
}
