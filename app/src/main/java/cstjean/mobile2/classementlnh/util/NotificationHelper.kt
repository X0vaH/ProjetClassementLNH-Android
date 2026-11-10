package cstjean.mobile2.classementlnh.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cstjean.mobile2.classementlnh.MainActivity
import cstjean.mobile2.classementlnh.R

/**
 * Centralise la création des canaux, la construction et l'affichage
 * de toutes les notifications de l'application.
 */
class NotificationHelper(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    /**
     * Crée tous les canaux de l'application. Appelée une seule fois,
     * au démarrage (ClassementLnhApplication.onCreate).
     */
    fun creerCanaux() {
        creerCanal(
            CHANNEL_EXPORT_ID, "Exportation",
            "Progression et résultat de l'exportation JSON du classement",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        creerCanal(
            CHANNEL_CLASSEMENT_ID, "Classement",
            "Changements de positions après une synchronisation du classement",
            NotificationManager.IMPORTANCE_HIGH
        )

        // L'importance d'un canal existant ne peut plus être modifiée par l'app :
        // on supprime l'ancien canal Classement (créé en IMPORTANCE_LOW).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(ANCIEN_CHANNEL_CLASSEMENT_ID)
        }
    }

    /**
     * Crée le canal de notification à partir des informations fournies.
     */
    private fun creerCanal(idCanal: String, nomCanal: String, descriptionCanal: String, importance: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(idCanal, nomCanal, importance).apply {
                description = descriptionCanal
            }
            notificationManager.createNotificationChannel(canal)
        }
    }

    /**
     * Vrai si l'application a le droit d'afficher des notifications :
     * permission POST_NOTIFICATIONS (Android 13+) et notifications non désactivées.
     */
    fun peutNotifier(): Boolean {
        val permissionAccordee = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        return permissionAccordee && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /**
     * Affiche la notification seulement si c'est permis. Ne lance jamais d'exception :
     * un Worker ou un service ne doit pas échouer à cause d'une notification.
     */
    private fun notifier(id: Int, notification: Notification) {
        if (!peutNotifier()) {
            Log.w(TAG, "Notifications non autorisées : notification $id ignorée.")
            return
        }
        try {
            notificationManager.notify(id, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Impossible d'afficher la notification $id", e)
        }
    }

    /**
     * Construit la notification de progression pour le service foreground.
     */
    fun construireNotificationProgression(texte: String, progres: Int): Notification {
        return NotificationCompat.Builder(context, CHANNEL_EXPORT_ID)
            .setContentTitle("Classement LNH — Exportation")
            .setContentText(texte)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setProgress(100, progres, progres == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Affiche ou met à jour la notification de progression.
     */
    fun notifierProgression(texte: String, progres: Int) {
        notifier(NOTIFICATION_ID_EXPORT, construireNotificationProgression(texte, progres))
    }

    /**
     * Affiche la notification de succès. Si un PendingIntent est fourni,
     * on ajoute l'action « Ouvrir le fichier ».
     */
    fun afficherSuccesExport(texte: String, intentOuvrirFichier: PendingIntent?) {
        val builder = NotificationCompat.Builder(context, CHANNEL_EXPORT_ID)
            .setContentTitle("Exportation réussie")
            .setContentText(texte)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            // La notification disparaît lorsqu'on la touche
            .setAutoCancel(true)

        intentOuvrirFichier?.let {
            builder.setContentIntent(it)
            builder.addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_menu_view,
                    "Ouvrir le fichier",
                    it
                ).build()
            )
        }

        notifier(NOTIFICATION_ID_RESULT, builder.build())
    }

    /**
     * Affiche la notification d'échec avec l'action « Réessayer ».
     */
    fun afficherErreurExport(texte: String, intentReessayer: PendingIntent) {
        val notificationErreur = NotificationCompat.Builder(context, CHANNEL_EXPORT_ID)
            .setContentTitle("Échec de l'exportation")
            .setContentText(texte)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(
                NotificationCompat.Action.Builder(
                    android.R.drawable.ic_popup_sync,
                    "Réessayer",
                    intentReessayer
                ).build()
            )
            .build()

        notifier(NOTIFICATION_ID_RESULT, notificationErreur)
    }

    /** Retire la notification de résultat (ex. : au moment de réessayer). */
    fun annulerResultatExport() {
        notificationManager.cancel(NOTIFICATION_ID_RESULT)
    }

    /**
     * Affiche le résumé des changements de classement. Toucher la notification
     * ouvre l'application et la fait disparaître.
     */
    fun afficherChangementClassement(nombreEquipesDeplacees: Int, nouveauChef: String?) {
        val texte = buildString {
            append(
                if (nombreEquipesDeplacees == 1) "1 équipe a changé de position."
                else "$nombreEquipesDeplacees équipes ont changé de position."
            )
            nouveauChef?.let { append(" Nouvelle équipe en tête : $it.") }
        }

        val intentOuvrirApp = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_OUVRIR_APP,
            intentOuvrirApp,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_CLASSEMENT_ID)
            .setContentTitle("Classement LNH mis à jour")
            .setContentText(texte)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texte))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notifier(NOTIFICATION_ID_CLASSEMENT, notification)
    }

    /**
     * Crée le canal de notification pour la synchronisation / push.
     */
    fun creerCanalPush() {
        creerCanal(
            CHANNEL_PUSH_ID, "Notifications push (Test Firebase)",
            "Canal utilisé pour les notifications push",
            NotificationManager.IMPORTANCE_DEFAULT)
    }

    /**
     * Affiche la notification reçue par Firebase.
     */
    fun afficherNotificationPush(titre: String, corps: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_PUSH_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(titre)
            .setContentText(corps)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notifier(NOTIFICATION_ID_PUSH, notification)
    }

    companion object {
        private const val TAG = "NotificationHelper"

        const val CHANNEL_EXPORT_ID = "canal_exportation"
        const val CHANNEL_CLASSEMENT_ID = "canal_classement_v2"
        private const val ANCIEN_CHANNEL_CLASSEMENT_ID = "canal_classement"

        // Nouveau canal pour les notifications push
        const val CHANNEL_PUSH_ID = "canal_push_notification"

        const val NOTIFICATION_ID_EXPORT = 1001
        const val NOTIFICATION_ID_RESULT = 1002
        const val NOTIFICATION_ID_CLASSEMENT = 1003

        // Nouvel id pour une notification push (1003 est déjà pris par le classement).
        const val NOTIFICATION_ID_PUSH = 1004

        private const val REQUEST_OUVRIR_APP = 2001
    }
}
