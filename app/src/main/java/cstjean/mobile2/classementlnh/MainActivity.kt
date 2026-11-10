package cstjean.mobile2.classementlnh

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import cstjean.mobile2.classementlnh.ui.navigation.LnhNavGraph
import cstjean.mobile2.classementlnh.ui.theme.ProjetDepartClassementLNHTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // On ne fait strictement rien si c'est accordé.
        if (!isGranted) {
            Log.i(TAG, "Permission de notification refusée.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Vérifier et demander la permission au démarrage (uniquement pour Android 13+)
        verifierEtDemanderPermissionNotification()

        // Enregistrement auprès de FCM : déclenche onRegistered() dans le service (log « FCM »)
        FirebaseMessaging.getInstance().register()

        @Suppress("DEPRECATION")
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            Log.d("FCM_TOKEN", "Mon token : $token")
        }

        setContent {
            ProjetDepartClassementLNHTheme {
                LnhNavGraph()
            }
        }
    }

    private fun verifierEtDemanderPermissionNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
