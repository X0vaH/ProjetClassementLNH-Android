package cstjean.mobile2.classementlnh

import android.app.Application
import cstjean.mobile2.classementlnh.data.remote.RetrofitInstance
import cstjean.mobile2.classementlnh.auth.AuthRetrofitInstance
import cstjean.mobile2.classementlnh.auth.SessionManager
import cstjean.mobile2.classementlnh.auth.repository.ApiAuthRepository
import cstjean.mobile2.classementlnh.database.local.AppDatabase
import cstjean.mobile2.classementlnh.repository.ClassementRepository
import cstjean.mobile2.classementlnh.util.NotificationHelper
import cstjean.mobile2.classementlnh.worker.SyncClassementWorker

class ClassementLnhApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val lnhApiService by lazy { RetrofitInstance.api }
    val repository by lazy {
        ClassementRepository(
            lnhApiService,
            database.classementDao(),
            database.syncMetadataDao()
        )
    }

    val sessionManager by lazy { SessionManager }
    val authApi by lazy { AuthRetrofitInstance.authApi }

    val authRepository: ApiAuthRepository by lazy { ApiAuthRepository(authApi, SessionManager) }

    override fun onCreate() {
        super.onCreate()
        // Création des canaux de notification, une seule fois au démarrage
        NotificationHelper(this).creerCanaux()

        // Initialisation du canal de notification via le Helper
        NotificationHelper(this).creerCanalPush()

        // Planification de la synchronisation périodique au démarrage de l'application
        SyncClassementWorker.planifier(this)
    }
}
