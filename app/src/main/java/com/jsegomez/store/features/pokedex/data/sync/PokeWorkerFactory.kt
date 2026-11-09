package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import com.jsegomez.store.features.pokedex.domain.usecase.RefreshPokemonListUseCase

// WorkManager crea los Worker con reflexión y solo sabe pasar (Context, WorkerParameters).
// Esta fábrica le enseña a construir PokeSyncWorker con sus dependencias reales.
class PokeWorkerFactory(
    private val refreshPokemonList: RefreshPokemonListUseCase,
    private val metadata: SyncMetadataStore,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        PokeSyncWorker::class.java.name ->
            PokeSyncWorker(appContext, workerParameters, refreshPokemonList, metadata)
        // null = "esta clase no es mía": WorkManager prueba con su fábrica por defecto.
        else -> null
    }
}
