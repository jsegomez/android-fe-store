package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.core.network.toAppError
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import com.jsegomez.store.features.pokedex.domain.usecase.RefreshPokemonListUseCase
import java.io.IOException

// Actualiza la lista de Pokémon en segundo plano.
// WorkManager puede crear este Worker cuando ya no existe ninguna pantalla, por eso sus
// dependencias llegan por constructor desde PokeWorkerFactory y no desde un ViewModel.
class PokeSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val refreshPokemonList: RefreshPokemonListUseCase,
    private val metadata: SyncMetadataStore,
) : CoroutineWorker(context, params) {

    // La cancelación NO se captura aquí: safeApiCall ya la relanza y debe llegar hasta
    // WorkManager. Convertirla en éxito o fallo anunciaría un resultado falso.
    override suspend fun doWork(): Result =
        refreshPokemonList().fold(
            onSuccess = {
                // Room ya guardó los datos (el repositorio escribe antes de devolver éxito).
                // Solo entonces se anota la fecha. Si anotarla falla, la caché sigue siendo
                // válida: se informa éxito igualmente y la fecha quedará algo desactualizada.
                try {
                    metadata.markSuccess(System.currentTimeMillis())
                } catch (_: IOException) {
                }
                Result.success()
            },
            onFailure = { error ->
                if (error.toAppError().isRetryable()) retryOrFail() else Result.failure()
            },
        )

    // runAttemptCount empieza en 0: se permiten los intentos 0, 1, 2 y 3 = 4 en total.
    private fun retryOrFail(): Result =
        if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()

    companion object {
        const val MAX_RETRIES = 3
    }
}

// Errores TEMPORALES: reintentar más tarde puede funcionar.
// Los permanentes (404, JSON incompatible, error desconocido) fallan sin reintentar:
// repetirlos solo gastaría batería y bombardearía la API.
internal fun AppError.isRetryable(): Boolean = when (this) {
    is AppError.NoConnection, is AppError.Timeout -> true
    is AppError.Http -> code == 429 || code in 500..599
    is AppError.Parsing, is AppError.Unknown -> false
}
