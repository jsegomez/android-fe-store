package com.jsegomez.store.features.pokedex.domain

import com.jsegomez.store.features.pokedex.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

// Contrato para pedir y observar la actualización persistente de la lista.
// El dominio solo conoce ESTE contrato; la implementación con WorkManager vive en data.
interface PokemonSyncScheduler {

    // Pide una actualización. Si ya hay una pendiente o en ejecución, no crea otra.
    fun requestSync()

    // Cancela la actualización pendiente o en ejecución. No borra la caché.
    fun cancelSync()

    // Estado actual del trabajo; se vuelve a emitir al recrear la pantalla o la app.
    fun observeSyncState(): Flow<SyncState>
}
