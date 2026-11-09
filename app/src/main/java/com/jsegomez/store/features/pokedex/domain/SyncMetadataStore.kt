package com.jsegomez.store.features.pokedex.domain

import kotlinx.coroutines.flow.Flow

// Metadatos NO sensibles de la sincronización (hoy, solo la fecha del último éxito).
// Es un contrato para que el Worker y las pruebas no dependan de DataStore.
interface SyncMetadataStore {

    // Milisegundos (epoch) del último refresh exitoso, o null si nunca hubo uno.
    fun observeLastSuccess(): Flow<Long?>

    suspend fun markSuccess(timeMillis: Long)
}
