package com.jsegomez.store.features.pokedex.domain.model

// Estado de la actualización en segundo plano, expresado en términos del dominio para que la
// pantalla no dependa de WorkManager (y se pueda probar sin él).
sealed interface SyncState {
    // No hay trabajo activo. No distingue por sí solo éxito, fallo o cancelación anterior.
    data object Idle : SyncState

    // Pendiente: espera conexión, un reintento (backoff) o que Android lo permita. NO es un fallo.
    data object Enqueued : SyncState

    data object Running : SyncState

    data object Succeeded : SyncState

    // Terminó sin éxito: error permanente o se agotaron los intentos.
    data object Failed : SyncState

    // El usuario lo canceló. No debe anunciarse como éxito.
    data object Cancelled : SyncState
}
