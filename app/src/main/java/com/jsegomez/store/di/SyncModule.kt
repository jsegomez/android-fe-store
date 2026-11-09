package com.jsegomez.store.di

import com.jsegomez.store.features.pokedex.data.sync.DataStoreSyncMetadataStore
import com.jsegomez.store.features.pokedex.data.sync.PokeWorkerFactory
import com.jsegomez.store.features.pokedex.data.sync.WorkManagerPokemonSyncScheduler
import com.jsegomez.store.features.pokedex.domain.PokemonSyncScheduler
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val syncModule = module {
    // single: DataStore permite una sola instancia por archivo
    single<SyncMetadataStore> { DataStoreSyncMetadataStore(androidContext()) }

    // get() resuelve RefreshPokemonListUseCase (useCaseModule) y SyncMetadataStore (arriba)
    single { PokeWorkerFactory(get(), get()) }

    single<PokemonSyncScheduler> { WorkManagerPokemonSyncScheduler(androidContext()) }
}
