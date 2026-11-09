package com.jsegomez.store

import android.app.Application
import androidx.work.Configuration
import com.jsegomez.store.di.databaseModule
import com.jsegomez.store.di.networkModule
import com.jsegomez.store.di.repositoryModule
import com.jsegomez.store.di.syncModule
import com.jsegomez.store.di.useCaseModule
import com.jsegomez.store.di.viewModelModule
import com.jsegomez.store.features.pokedex.data.sync.PokeWorkerFactory
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// Configuration.Provider: WorkManager le pide su configuración a la Application la primera vez que
// se usa. Así podemos darle la fábrica con dependencias de Koin. Esto solo funciona porque en el
// manifest se desactivó su inicialización automática (WorkManagerInitializer).
class StoreApplication : Application(), Configuration.Provider {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StoreApplication)
            modules(
                networkModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                syncModule,
                viewModelModule,
            )
        }
    }

    // get (no un valor fijo): se evalúa de forma diferida, cuando Koin ya arrancó en onCreate.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(getKoin().get<PokeWorkerFactory>())
            .build()
}
