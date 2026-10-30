package com.jsegomez.store

import android.app.Application
import com.jsegomez.store.di.networkModule
import com.jsegomez.store.di.repositoryModule
import com.jsegomez.store.di.useCaseModule
import com.jsegomez.store.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StoreApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StoreApplication)
            modules(networkModule, repositoryModule, useCaseModule, viewModelModule)
        }
    }
}
