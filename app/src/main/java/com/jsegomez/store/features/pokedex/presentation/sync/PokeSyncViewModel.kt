package com.jsegomez.store.features.pokedex.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jsegomez.store.features.pokedex.domain.PokemonSyncScheduler
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Este ViewModel NO ejecuta la actualización: solo PIDE el trabajo y muestra su estado.
// El trabajo vive en WorkManager, así que sigue aunque este ViewModel se destruya.
class PokeSyncViewModel(
    private val scheduler: PokemonSyncScheduler,
    metadata: SyncMetadataStore,
) : ViewModel() {

    // El estado NO se guarda aquí: se reconstruye desde WorkManager y DataStore cada vez que la
    // pantalla vuelve a observar. Por eso la UI se recupera tras rotar o recrear la app.
    val uiState: StateFlow<SyncUiState> = combine(
        scheduler.observeSyncState(),
        metadata.observeLastSuccess(),
    ) { state, lastSuccess -> SyncUiState(state = state, lastSuccessMillis = lastSuccess) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = SyncUiState(),
        )

    fun onSyncClick() = scheduler.requestSync()

    fun onCancelClick() = scheduler.cancelSync()

    private companion object {
        // Margen para no reiniciar la observación en una rotación de pantalla
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
