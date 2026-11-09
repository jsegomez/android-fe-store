package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.jsegomez.store.features.pokedex.domain.PokemonSyncScheduler
import com.jsegomez.store.features.pokedex.domain.model.SyncState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class WorkManagerPokemonSyncScheduler(context: Context) : PokemonSyncScheduler {

    private val appContext = context.applicationContext

    // lazy: WorkManager se inicializa con la configuración de StoreApplication, que necesita
    // Koin ya arrancado. Pedirlo al construir este objeto podría adelantarse a ese arranque.
    private val workManager by lazy { WorkManager.getInstance(appContext) }

    override fun requestSync() {
        val request = OneTimeWorkRequestBuilder<PokeSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    // CONNECTED = hay una red, NO garantiza que PokéAPI responda: por eso el
                    // Worker igualmente clasifica y reintenta los fallos de red.
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            // Espera entre reintentos: 30 s, 60 s, 120 s... (se duplica cada vez)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, BACKOFF_SECONDS, TimeUnit.SECONDS)
            .build()

        // KEEP: si ya hay un trabajo con este nombre pendiente o en ejecución, se conserva y esta
        // solicitud se descarta. Varias pulsaciones seguidas = un solo trabajo activo.
        // Al terminar el anterior, una solicitud nueva sí puede empezar.
        workManager.enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    // Cancela el trabajo activo. No borra la caché: los datos ya guardados siguen en Room.
    override fun cancelSync() {
        workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    // Se vuelve a suscribir al nombre único: tras recrear la pantalla o la app, el estado
    // se reconstruye desde WorkManager sin que la UI tenga que recordar nada.
    override fun observeSyncState(): Flow<SyncState> =
        workManager.getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
            .map { infos ->
                // Prioridad al trabajo activo; si no hay, el último resultado conocido.
                // WorkManager puede podar su historial: es un estado de pantalla, no un registro.
                val info = infos.firstOrNull { !it.state.isFinished } ?: infos.lastOrNull()
                info?.state.toSyncState()
            }
            .distinctUntilChanged()

    private fun WorkInfo.State?.toSyncState(): SyncState = when (this) {
        null -> SyncState.Idle
        // BLOCKED solo ocurre en cadenas con dependencias; aquí se trataría como pendiente.
        WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> SyncState.Enqueued
        WorkInfo.State.RUNNING -> SyncState.Running
        WorkInfo.State.SUCCEEDED -> SyncState.Succeeded
        WorkInfo.State.FAILED -> SyncState.Failed
        WorkInfo.State.CANCELLED -> SyncState.Cancelled
    }

    companion object {
        const val UNIQUE_WORK_NAME = "poke-cache-sync"
        private const val BACKOFF_SECONDS = 30L
    }
}
