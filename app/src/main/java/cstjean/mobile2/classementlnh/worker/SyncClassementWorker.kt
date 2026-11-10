package cstjean.mobile2.classementlnh.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Worker qui récupère le classement distant (API LNH), met à jour Room,
 * puis notifie l'utilisateur si des équipes ont changé de position.
 */
class SyncClassementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Début de la synchronisation du classement LNH (tentative ${runAttemptCount + 1})")

        val repository = (applicationContext as ClassementLnhApplication).repository

        try {
            val avant = repository.obtenirClassementPourExport()
            repository.synchroniser()
            val apres = repository.obtenirClassementPourExport()
            Log.d(
                TAG,
                "Synchronisation réussie. Avant : ${avant.take(3).map { "${it.abbrev}=${it.points}" }}" +
                    " / Après : ${apres.take(3).map { "${it.abbrev}=${it.points}" }}"
            )

            notifierSiChangement(avant, apres)
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

    /**
     * Notifie seulement s'il y a au moins un changement de position.
     * Ne lance jamais d'exception : une notification ne doit pas faire échouer le Worker.
     */
    private fun notifierSiChangement(avant: List<EquipeUiModel>, apres: List<EquipeUiModel>) {
        try {
            val changement = comparerClassements(avant, apres)
            if (changement.nombreEquipesDeplacees == 0) {
                Log.d(TAG, "Aucun changement de position : pas de notification.")
                return
            }
            Log.d(TAG, "Changement de classement : $changement")
            // Le helper vérifie la permission; sans elle, rien ne s'affiche.
            NotificationHelper(applicationContext).afficherChangementClassement(
                changement.nombreEquipesDeplacees,
                changement.nouveauChef
            )
        } catch (e: Exception) {
            Log.w(TAG, "Impossible de notifier le changement de classement", e)
        }
    }

    /** Résumé de la comparaison entre deux classements. */
    data class ChangementClassement(
        val nombreEquipesDeplacees: Int,
        /** Nom de la nouvelle équipe en tête, ou null si la tête n'a pas changé. */
        val nouveauChef: String?
    )

    companion object {
        private const val TAG = "SyncClassementWorker"
        private const val MAX_TENTATIVES = 3
        const val NOM_TRAVAIL_UNIQUE = "SyncClassementUnique"
        const val NOM_TRAVAIL_IMMEDIAT = "SyncClassementImmediat"

        /**
         * Compare deux classements (listes déjà triées par position).
         * Seules les équipes présentes avant et après sont comparées : lors de la
         * toute première synchronisation (classement vide), il n'y a aucun changement.
         */
        fun comparerClassements(
            avant: List<EquipeUiModel>,
            apres: List<EquipeUiModel>
        ): ChangementClassement {
            if (avant.isEmpty()) return ChangementClassement(0, null)

            val positionsAvant = avant.mapIndexed { index, equipe -> equipe.abbrev to index }.toMap()
            val deplacees = apres.withIndex().count { (index, equipe) ->
                val ancienne = positionsAvant[equipe.abbrev]
                ancienne != null && ancienne != index
            }

            val chefAvant = avant.firstOrNull()?.abbrev
            val chefApres = apres.firstOrNull()
            val nouveauChef = chefApres?.takeIf { it.abbrev != chefAvant }?.nom

            return ChangementClassement(deplacees, nouveauChef)
        }

        /**
         * Planifie la synchronisation périodique (toutes les 4 heures) avec une
         * contrainte de connexion réseau. Le travail unique + KEEP évite de
         * planifier plusieurs synchronisations identiques.
         */
        fun planifier(context: Context) {
            val requete = PeriodicWorkRequestBuilder<SyncClassementWorker>(4, TimeUnit.HOURS)
                .setConstraints(contraintes())
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                NOM_TRAVAIL_UNIQUE,
                ExistingPeriodicWorkPolicy.KEEP,
                requete
            )
        }

        /**
         * Bouton de test « Synchroniser maintenant » : lance le même Worker
         * comme tâche unique (OneTimeWorkRequest).
         */
        fun synchroniserMaintenant(context: Context) {
            val requete = OneTimeWorkRequestBuilder<SyncClassementWorker>()
                .setConstraints(contraintes())
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                NOM_TRAVAIL_IMMEDIAT,
                ExistingWorkPolicy.REPLACE,
                requete
            )
        }

        private fun contraintes() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}
