package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import com.jsegomez.store.features.pokedex.domain.usecase.RefreshPokemonListUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

// Prueba la LÓGICA de doWork() con un repositorio falso. No demuestra que Android programe el
// trabajo con red real, batería o process death: eso se comprueba a mano en el emulador.
// Pasa por PokeWorkerFactory, así que también comprueba que la fábrica construye el Worker.
@RunWith(AndroidJUnit4::class)
class PokeSyncWorkerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val repository = FakeRepository()
    private val metadata = FakeMetadata()

    private fun worker(runAttemptCount: Int = 0) =
        TestListenableWorkerBuilder<PokeSyncWorker>(context)
            .setWorkerFactory(PokeWorkerFactory(RefreshPokemonListUseCase(repository), metadata))
            .setRunAttemptCount(runAttemptCount)
            .build()

    @Test
    fun success_returnsSuccess_andSavesTheDate() = runBlocking {
        val result = worker().doWork()

        assertEquals(ListenableWorker.Result.success(), result)
        assertTrue(metadata.lastSuccess.value != null)
        assertEquals(1, repository.refreshCalls)
    }

    @Test
    fun temporaryFailure_retries_andDoesNotSaveTheDate() = runBlocking {
        repository.result = Result.failure(AppError.NoConnection(IOException()))

        val result = worker(runAttemptCount = 0).doWork()

        assertEquals(ListenableWorker.Result.retry(), result)
        assertNull(metadata.lastSuccess.value)
    }

    @Test
    fun serverErrorAnd429_retry() = runBlocking {
        repository.result = Result.failure(AppError.Http(503, IOException()))
        assertEquals(ListenableWorker.Result.retry(), worker().doWork())

        repository.result = Result.failure(AppError.Http(429, IOException()))
        assertEquals(ListenableWorker.Result.retry(), worker().doWork())
    }

    // El contador empieza en 0: los intentos 0, 1, 2 y 3 reintentan o ejecutan = 4 en total
    @Test
    fun retriesAreAllowedUpToTheThirdAttempt_thenItFails() = runBlocking {
        repository.result = Result.failure(AppError.Timeout(IOException()))

        assertEquals(ListenableWorker.Result.retry(), worker(runAttemptCount = 2).doWork())
        assertEquals(ListenableWorker.Result.failure(), worker(runAttemptCount = 3).doWork())
    }

    @Test
    fun permanentHttpError_failsWithoutRetrying() = runBlocking {
        repository.result = Result.failure(AppError.Http(404, IOException()))

        assertEquals(ListenableWorker.Result.failure(), worker(runAttemptCount = 0).doWork())
        assertNull(metadata.lastSuccess.value)
    }

    @Test
    fun incompatibleJson_failsWithoutRetrying() = runBlocking {
        repository.result = Result.failure(AppError.Parsing(IOException()))

        assertEquals(ListenableWorker.Result.failure(), worker(runAttemptCount = 0).doWork())
    }

    @Test
    fun cancellation_isPropagated_notTurnedIntoSuccessOrFailure() = runBlocking {
        repository.throwable = CancellationException("cancelado")

        try {
            worker().doWork()
            fail("La cancelación debía propagarse")
        } catch (expected: CancellationException) {
            // Correcto: no se convirtió en un Result
        }
        assertNull(metadata.lastSuccess.value)
    }
}

private class FakeRepository : PokedexRepository {
    var result: Result<Unit> = Result.success(Unit)
    var throwable: Throwable? = null
    var refreshCalls = 0

    override suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit> {
        refreshCalls++
        throwable?.let { throw it }
        return result
    }

    override fun observePokemonList(): Flow<List<PokemonSummary>> = emptyFlow()

    override fun observePokemonDetail(id: Int): Flow<PokemonDetail?> = emptyFlow()

    override suspend fun refreshPokemonDetail(id: Int): Result<Unit> = Result.success(Unit)
}

private class FakeMetadata : SyncMetadataStore {
    val lastSuccess = MutableStateFlow<Long?>(null)

    override fun observeLastSuccess(): Flow<Long?> = lastSuccess

    override suspend fun markSuccess(timeMillis: Long) {
        lastSuccess.value = timeMillis
    }
}
