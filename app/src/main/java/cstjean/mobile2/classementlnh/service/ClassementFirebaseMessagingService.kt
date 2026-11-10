package cstjean.mobile2.classementlnh.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import cstjean.mobile2.classementlnh.util.NotificationHelper

// onNewToken() est déprécié depuis Firebase Messaging 26 : onRegistered() le remplace.
// Lint ne connaît pas encore la nouvelle API, d'où la suppression de l'avertissement.
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class ClassementFirebaseMessagingService : FirebaseMessagingService() {
    // Initialisation paresseuse du helper grâce au contexte du Service
    private val notificationHelper by lazy { NotificationHelper(this) }

    // Appelé lorsqu'un message push est reçu alors que l'app est au premier plan
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Si le message contient une charge utile (payload) de notification
        remoteMessage.notification?.let { notification ->
            afficherNotificationPush(
                titre = notification.title ?: "Notification Classement LNH",
                corps = notification.body ?: "Nouveau contenu disponible"
            )
        }
    }

    override fun onRegistered(installationId: String) {
        super.onRegistered(installationId)
        Log.d("FCM", installationId)
    }

    private fun afficherNotificationPush(titre: String, corps: String) {
        val context = applicationContext // ou votre contexte

        // Vérification de la permission pour Android 13+
        val permissionCheck = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Sous Android 12 et inférieur, la permission est accordée par défaut
        }

        if (permissionCheck) {
            // On réutilise la logique d'affichage locale qu'on a vue précédemment !
            notificationHelper.afficherNotificationPush(titre, corps)
        } else {
            // Optionnel : La permission n'est pas accordée, on ne fait rien ou on log un avertissement
            Log.w("Notification", "Impossible d'afficher la notification : permission refusée.")
        }
    }
}
