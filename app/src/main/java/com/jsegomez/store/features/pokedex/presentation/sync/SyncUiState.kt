package com.jsegomez.store.features.pokedex.presentation.sync

import com.jsegomez.store.features.pokedex.domain.model.SyncState

data class SyncUiState(
    // Qué está pasando con el trabajo en segundo plano (viene de WorkManager)
    val state: SyncState = SyncState.Idle,
    // Fecha del último refresh exitoso (viene de DataStore). null = nunca ha habido uno.
    val lastSuccessMillis: Long? = null,
) {
    // Pendiente o ejecutándose: hay algo que cancelar y no tiene sentido pedir otra actualización
    val isActive: Boolean
        get() = state == SyncState.Enqueued || state == SyncState.Running
}
