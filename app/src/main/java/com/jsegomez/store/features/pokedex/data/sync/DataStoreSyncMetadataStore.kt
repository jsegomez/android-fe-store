package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore exige UNA sola instancia por archivo: por eso el delegado vive a nivel de archivo
// y no dentro de la clase. Guarda un dato pequeño y público; no se cifra porque no es sensible.
private val Context.syncDataStore by preferencesDataStore(name = "sync_metadata")

class DataStoreSyncMetadataStore(context: Context) : SyncMetadataStore {

    // applicationContext: el store vive tanto como el proceso, no como una Activity.
    private val dataStore = context.applicationContext.syncDataStore

    override fun observeLastSuccess(): Flow<Long?> =
        dataStore.data.map { prefs -> prefs[LAST_SUCCESS] }

    override suspend fun markSuccess(timeMillis: Long) {
        dataStore.edit { prefs -> prefs[LAST_SUCCESS] = timeMillis }
    }

    private companion object {
        val LAST_SUCCESS = longPreferencesKey("last_success_millis")
    }
}
