package cstjean.mobile2.classementlnh.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.google.gson.GsonBuilder
import cstjean.mobile2.classementlnh.ClassementLnhApplication
import cstjean.mobile2.classementlnh.R
import cstjean.mobile2.classementlnh.business.EquipeUiModel
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
 */
class ExportService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        creerCanalNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 1. Démarrer immédiatement au premier plan
        val notificationInitiale = construireNotification("Préparation de l'exportation...", 0, enCours = true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(NOTIFICATION_ID, notificationInitiale, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(NOTIFICATION_ID, notificationInitiale)
        }

        // 2. Traitement d'exportation
        serviceScope.launch {
            var messageFinal: String
            try {
                val repository = (applicationContext as ClassementLnhApplication).repository

                // Les délais espacent les notifications : Android ignore celles
                // publiées trop rapidement (limite d'environ 5 par seconde).
                delay(DELAI_ETAPE_MS)
                mettreAJourNotification("Lecture du classement dans la base de données...", 20)
                val equipes = repository.obtenirClassementPourExport()

                delay(DELAI_ETAPE_MS)
                mettreAJourNotification("Génération du fichier JSON...", 60)
                val nomFichier = genererEtSauvegarderJson(equipes)

                delay(DELAI_ETAPE_MS)
                messageFinal = "Exportation réussie : Download/$nomFichier (${equipes.size} équipes)"
                Log.d(TAG, messageFinal)
            } catch (e: Exception) {
                Log.e(TAG, "Échec de l'exportation", e)
                messageFinal = "Échec de l'exportation : ${e.localizedMessage ?: "erreur inconnue"}"
            }

            // 3. Fin : on retire la notification de premier plan et on affiche le résultat
            ServiceCompat.stopForeground(this@ExportService, ServiceCompat.STOP_FOREGROUND_REMOVE)
            afficherNotificationResultat(messageFinal)
            afficherToast(messageFinal)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    /**
     * Produit le JSON (équipes + statistiques) et l'enregistre dans Download.
     * Retourne le nom du fichier créé.
     */
    private suspend fun genererEtSauvegarderJson(equipes: List<EquipeUiModel>): String =
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ : MediaStore, aucune permission de stockage requise
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
            } else {
                // Android 9 et moins : dossier Download propre à l'application (sans permission)
                val dossier = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    ?: throw IllegalStateException("Stockage externe indisponible")
                if (!dossier.exists()) dossier.mkdirs()
                File(dossier, nomFichier).writeBytes(octets)
            }

            nomFichier
        }

    private fun creerCanalNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CHANNEL_ID,
                "Exportation du classement",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Progression et résultat de l'exportation JSON du classement LNH"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal)
        }
    }

    private fun construireNotification(texte: String, progres: Int, enCours: Boolean): Notification {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Classement LNH — Exportation")
            .setContentText(texte)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(enCours)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        if (enCours) {
            builder.setProgress(100, progres, progres == 0)
        } else {
            builder.setAutoCancel(true)
        }
        return builder.build()
    }

    private fun mettreAJourNotification(texte: String, progres: Int) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, construireNotification(texte, progres, enCours = true))
    }

    private fun afficherNotificationResultat(texte: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_RESULTAT_ID, construireNotification(texte, 100, enCours = false))
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
        const val CHANNEL_ID = "export_classement_channel"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_RESULTAT_ID = 1002
    }
}
