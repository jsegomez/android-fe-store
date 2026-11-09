package com.jsegomez.store.features.pokedex.presentation.pokedex

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

// viewModelScope trabaja en el hilo principal, que no existe en un test de JVM.
// UnconfinedTestDispatcher lo sustituye y ejecuta las corrutinas de inmediato,
// así que aquí se puede leer uiState.value justo después de cada acción.
@OptIn(ExperimentalCoroutinesApi::class)
class PokedexViewModelTest {
    private val repository = FakePokedexRepository()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_showsSavedPokemon_andStopsLoading() {
        repository.saved.value = listOf(BULBASAUR)

        val state = PokedexViewModel(repository).uiState.value

        assertEquals(listOf(BULBASAUR), state.pokemon)
        assertFalse(state.isLoading)
    }

    @Test
    fun init_withEmptyDatabase_stopsLoading() {
        // La primera lectura de Room puede ser una lista vacía y aun así terminó de cargar
        val state = PokedexViewModel(repository).uiState.value

        assertTrue(state.pokemon.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun refreshFailure_keepsSavedPokemonVisible_andReportsTheError() {
        repository.saved.value = listOf(BULBASAUR)
        repository.refreshResult = Result.failure(AppError.NoConnection(IOException()))

        val state = PokedexViewModel(repository).uiState.value

        assertEquals(listOf(BULBASAUR), state.pokemon)
        assertTrue(state.refreshError is AppError.NoConnection)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun retry_afterSuccessfulRefresh_clearsThePreviousRefreshError() {
        repository.refreshResult = Result.failure(AppError.NoConnection(IOException()))
        val viewModel = PokedexViewModel(repository)
        assertNotNull(viewModel.uiState.value.refreshError)

        repository.refreshResult = Result.success(Unit)
        viewModel.retry()

        assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun localReadFailure_isReportedInsteadOfCrashing_andRetryRecovers() {
        repository.observation = flow { throw IOException("disco ilegible") }
        val viewModel = PokedexViewModel(repository)
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)

        // La lectura vuelve a funcionar: "Reintentar" debe reiniciar la observación
        repository.saved.value = listOf(BULBASAUR)
        repository.observation = repository.saved
        viewModel.retry()

        assertNull(viewModel.uiState.value.error)
        assertEquals(listOf(BULBASAUR), viewModel.uiState.value.pokemon)
    }

    private companion object {
        val BULBASAUR = PokemonSummary(id = 1, name = "bulbasaur", imageUrl = "https://example.com/1.png")
    }
}

// Repositorio falso: lo guardado y el resultado del refresh los controla cada test
private class FakePokedexRepository : PokedexRepository {
    val saved = MutableStateFlow<List<PokemonSummary>>(emptyList())
    var observation: Flow<List<PokemonSummary>> = saved
    var refreshResult: Result<Unit> = Result.success(Unit)

    override fun observePokemonList(): Flow<List<PokemonSummary>> = observation

    override suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit> = refreshResult

    // El detalle no se usa en estas pruebas
    override fun observePokemonDetail(id: Int): Flow<PokemonDetail?> = emptyFlow()

    override suspend fun refreshPokemonDetail(id: Int): Result<Unit> = Result.success(Unit)
}
