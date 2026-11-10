package cstjean.mobile2.classementlnh.service

import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.gson.GsonBuilder
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import cstjean.mobile2.classementlnh.business.EquipeUiModel
import cstjean.mobile2.classementlnh.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Service au premier plan qui lit le classement dans Room et l'exporte
 * en JSON dans le répertoire Download de l'appareil.
 * Les notifications passent toutes par le NotificationHelper (aucun canal créé ici).
 */
class ExportService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private lateinit var notificationHelper: NotificationHelper

    /** Résultat d'une exportation réussie. */
    private data class FichierExporte(val nom: String, val uri: Uri)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        // Les canaux sont déjà créés au démarrage de l'application
        notificationHelper = NotificationHelper(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val simulerEchec = intent?.getBooleanExtra(EXTRA_SIMULER_ECHEC, false) ?: false

        // Une nouvelle tentative remplace l'ancien résultat (ex. : après « Réessayer »)
        notificationHelper.annulerResultatExport()

        // 1. Démarrer immédiatement au premier plan
        val notificationInitiale =
            notificationHelper.construireNotificationProgression("Préparation de l'exportation...", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NotificationHelper.NOTIFICATION_ID_EXPORT,
                notificationInitiale,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NotificationHelper.NOTIFICATION_ID_EXPORT, notificationInitiale)
        }

        // 2. Traitement d'exportation
        serviceScope.launch {
            try {
                val repository = (applicationContext as ClassementLnhApplication).repository

                // Les délais espacent les notifications : Android ignore celles
                // publiées trop rapidement (limite d'environ 5 par seconde).
                delay(DELAI_ETAPE_MS)
                notificationHelper.notifierProgression("Lecture du classement dans la base de données...", 20)
                val equipes = repository.obtenirClassementPourExport()
                if (equipes.isEmpty()) {
                    throw IllegalStateException("Aucune donnée à exporter : synchronisez d'abord le classement.")
                }

                delay(DELAI_ETAPE_MS)
                notificationHelper.notifierProgression("Génération du fichier JSON...", 60)
                if (simulerEchec) {
                    throw IllegalStateException("Échec simulé pour tester l'action « Réessayer ».")
                }
                val fichier = genererEtSauvegarderJson(equipes)

                delay(DELAI_ETAPE_MS)
                val message = "Download/${fichier.nom} (${equipes.size} équipes)"
                Log.d(TAG, "Exportation réussie : $message")

                // 3. Fin : on retire la notification de premier plan et on affiche le résultat
                ServiceCompat.stopForeground(this@ExportService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                notificationHelper.afficherSuccesExport(message, genererIntentOuvrirFichier(fichier.uri))
                afficherToast("Exportation réussie : $message")
            } catch (e: Exception) {
                Log.e(TAG, "Échec de l'exportation", e)
                val message = e.localizedMessage ?: "Erreur inconnue"

                ServiceCompat.stopForeground(this@ExportService, ServiceCompat.STOP_FOREGROUND_REMOVE)
                notificationHelper.afficherErreurExport(message, genererIntentReessayer())
                afficherToast("Échec de l'exportation : $message")
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * PendingIntent de l'action « Réessayer » : relance ce service d'exportation
     * (sans l'option d'échec simulé, donc une exportation normale).
     */
    private fun genererIntentReessayer(): PendingIntent {
        val intent = Intent(this, ExportService::class.java)
        val drapeaux = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, REQUEST_REESSAYER, intent, drapeaux)
        } else {
            PendingIntent.getService(this, REQUEST_REESSAYER, intent, drapeaux)
        }
    }

    /**
     * PendingIntent de l'action « Ouvrir le fichier ». L'ouverture ne fonctionne que
     * si une application capable de lire un fichier JSON est installée.
     */
    private fun genererIntentOuvrirFichier(uri: Uri): PendingIntent {
        val intentOuvrir = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/json")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return PendingIntent.getActivity(
            this,
            REQUEST_OUVRIR_FICHIER,
            Intent.createChooser(intentOuvrir, "Ouvrir l'exportation").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Produit le JSON (équipes + statistiques) et l'enregistre dans Download.
     * Retourne le nom du fichier et son Uri (partageable avec d'autres applications).
     */
    private suspend fun genererEtSauvegarderJson(equipes: List<EquipeUiModel>): FichierExporte =
        withContext(Dispatchers.IO) {
            val horodatage = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CANADA_FRENCH).format(Date())
            val nomFichier = "classement_lnh_$horodatage.json"

            val export = mapOf(
                "dateExportation" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CANADA_FRENCH).format(Date()),
                "nombreEquipes" to equipes.size,
                "equipes" to equipes
            )
            val contenu = GsonBuilder().setPrettyPrinting().create().toJson(export)
            val octets = contenu.toByteArray(Charsets.UTF_8)

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ : MediaStore, aucune permission de stockage requise.
                // L'Uri content:// retourné peut être partagé directement.
                val valeurs = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, nomFichier)
                    put(MediaStore.Downloads.MIME_TYPE, "application/json")
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, valeurs)
                    ?: throw IllegalStateException("Impossible de créer le fichier dans Download")

                resolver.openOutputStream(uri)?.use { it.write(octets) }
                    ?: throw IllegalStateException("Impossible d'écrire le fichier")

                valeurs.clear()
                valeurs.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(uri, valeurs, null, null)
                uri
            } else {
                // Android 9 et moins : dossier Download propre à l'application (sans permission).
                // Le FileProvider sert de passerelle sécurisée pour partager le fichier.
                val dossier = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: throw IllegalStateException("Stockage externe indisponible")
                if (!dossier.exists()) dossier.mkdirs()
                val fichier = File(dossier, nomFichier)
                fichier.writeBytes(octets)
                FileProvider.getUriForFile(this@ExportService, "$packageName.fileprovider", fichier)
            }

            FichierExporte(nomFichier, uri)
        }

    /** Message visible même si la permission de notification est refusée. */
    private fun afficherToast(texte: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(applicationContext, texte, Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        private const val TAG = "ExportService"
        private const val DELAI_ETAPE_MS = 700L
        private const val REQUEST_REESSAYER = 3001
        private const val REQUEST_OUVRIR_FICHIER = 3002

        /** Extra de test : force un échec pour vérifier l'action « Réessayer ». */
        const val EXTRA_SIMULER_ECHEC = "EXTRA_SIMULER_ECHEC"

        fun demarrer(context: Context, simulerEchec: Boolean = false) {
            val intent = Intent(context, ExportService::class.java)
                .putExtra(EXTRA_SIMULER_ECHEC, simulerEchec)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
