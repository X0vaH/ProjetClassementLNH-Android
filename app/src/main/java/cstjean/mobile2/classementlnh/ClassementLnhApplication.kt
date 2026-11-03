package cstjean.mobile2.classementlnh

import android.app.Application
import cstjean.mobile2.classementlnh.data.remote.RetrofitInstance
import cstjean.mobile2.classementlnh.database.local.AppDatabase
import cstjean.mobile2.classementlnh.repository.ClassementRepository

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

    override fun onCreate() {
        super.onCreate()
    }
}