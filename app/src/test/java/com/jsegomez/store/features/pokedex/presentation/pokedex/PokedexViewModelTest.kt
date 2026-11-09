package com.jsegomez.store.features.pokedex.presentation.pokedex

import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.features.pokedex.domain.FakeFavoriteRepository
import com.jsegomez.store.features.pokedex.domain.PokedexRepository
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import com.jsegomez.store.features.pokedex.domain.usecase.ObserveFavoritePokemonUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
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
    private val favorites = FakeFavoriteRepository(catalog = listOf(BULBASAUR, PIKACHU))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() =
        PokedexViewModel(repository, ObserveFavoritePokemonUseCase(favorites))

    // ---------- Lista y refresh ----------

    @Test
    fun init_showsSavedPokemon_andStopsLoading() {
        repository.saved.value = listOf(BULBASAUR)

        val state = createViewModel().uiState.value

        assertEquals(listOf(BULBASAUR), state.pokemon)
        assertFalse(state.isLoading)
    }

    @Test
    fun init_withEmptyDatabase_stopsLoading() {
        // La primera lectura de Room puede ser una lista vacía y aun así terminó de cargar
        val state = createViewModel().uiState.value

        assertTrue(state.pokemon.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun refreshFailure_keepsSavedPokemonVisible_andReportsTheError() {
        repository.saved.value = listOf(BULBASAUR)
        repository.refreshResult = Result.failure(AppError.NoConnection(IOException()))

        val state = createViewModel().uiState.value

        assertEquals(listOf(BULBASAUR), state.pokemon)
        assertTrue(state.refreshError is AppError.NoConnection)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun retry_afterSuccessfulRefresh_clearsThePreviousRefreshError() {
        repository.refreshResult = Result.failure(AppError.NoConnection(IOException()))
        val viewModel = createViewModel()
        assertNotNull(viewModel.uiState.value.refreshError)

        repository.refreshResult = Result.success(Unit)
        viewModel.retry()

        assertNull(viewModel.uiState.value.refreshError)
    }

    @Test
    fun localReadFailure_isReportedInsteadOfCrashing_andRetryRecovers() {
        repository.observation = flow { throw IOException("disco ilegible") }
        val viewModel = createViewModel()
        assertNotNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)

        // La lectura vuelve a funcionar: "Reintentar" debe reiniciar la observación
        repository.saved.value = listOf(BULBASAUR)
        repository.observation = repository.saved
        viewModel.retry()

        assertNull(viewModel.uiState.value.error)
        assertEquals(listOf(BULBASAUR), viewModel.uiState.value.pokemon)
    }

    // ---------- Filtro "Solo favoritos" ----------

    @Test
    fun onlyFavorites_isOffByDefault_andShowsEveryPokemon() {
        repository.saved.value = listOf(BULBASAUR, PIKACHU)

        val state = createViewModel().uiState.value

        assertFalse(state.onlyFavorites)
        assertEquals(listOf(BULBASAUR, PIKACHU), state.pokemon)
    }

    @Test
    fun onlyFavorites_showsJustTheMarkedPokemon() {
        repository.saved.value = listOf(BULBASAUR, PIKACHU)
        runBlocking { favorites.toggle(PIKACHU.id) }
        val viewModel = createViewModel()

        viewModel.onToggleOnlyFavorites()

        assertTrue(viewModel.uiState.value.onlyFavorites)
        assertEquals(listOf(PIKACHU), viewModel.uiState.value.pokemon)
    }

    @Test
    fun onlyFavorites_toggledAgain_showsEveryPokemonAgain() {
        repository.saved.value = listOf(BULBASAUR, PIKACHU)
        runBlocking { favorites.toggle(PIKACHU.id) }
        val viewModel = createViewModel()

        viewModel.onToggleOnlyFavorites()
        viewModel.onToggleOnlyFavorites()

        assertFalse(viewModel.uiState.value.onlyFavorites)
        assertEquals(listOf(BULBASAUR, PIKACHU), viewModel.uiState.value.pokemon)
    }

    @Test
    fun onlyFavorites_withNoFavorites_isEmptyButNotLoading() {
        repository.saved.value = listOf(BULBASAUR, PIKACHU)
        val viewModel = createViewModel()

        viewModel.onToggleOnlyFavorites()

        // "Sin favoritos" tiene que distinguirse de "todavía cargando" y de "lista sin descargar"
        val state = viewModel.uiState.value
        assertTrue(state.onlyFavorites)
        assertTrue(state.pokemon.isEmpty())
        assertFalse(state.isLoading)
    }

    @Test
    fun onlyFavorites_followsFavoritesMarkedWhileTheFilterIsOn() {
        repository.saved.value = listOf(BULBASAUR, PIKACHU)
        val viewModel = createViewModel()
        viewModel.onToggleOnlyFavorites()

        runBlocking { favorites.toggle(BULBASAUR.id) }

        assertEquals(listOf(BULBASAUR), viewModel.uiState.value.pokemon)
    }

    @Test
    fun onlyFavorites_doesNotAskThePokeApiForAnything() {
        val viewModel = createViewModel()
        assertEquals(1, repository.refreshCalls) // la descarga normal de al abrir la pantalla

        viewModel.onToggleOnlyFavorites()
        viewModel.onToggleOnlyFavorites()

        // El filtro solo cambia qué se lee de Room: funciona igual sin internet
        assertEquals(1, repository.refreshCalls)
    }

    private companion object {
        val BULBASAUR = PokemonSummary(id = 1, name = "bulbasaur", imageUrl = "https://example.com/1.png")
        val PIKACHU = PokemonSummary(id = 25, name = "pikachu", imageUrl = "https://example.com/25.png")
    }
}

// Repositorio falso: lo guardado y el resultado del refresh los controla cada test
private class FakePokedexRepository : PokedexRepository {
    val saved = MutableStateFlow<List<PokemonSummary>>(emptyList())
    var observation: Flow<List<PokemonSummary>> = saved
    var refreshResult: Result<Unit> = Result.success(Unit)
    var refreshCalls = 0

    override fun observePokemonList(): Flow<List<PokemonSummary>> = observation

    override suspend fun refreshPokemonList(limit: Int, offset: Int): Result<Unit> {
        refreshCalls++
        return refreshResult
    }

    // El detalle no se usa en estas pruebas
    override fun observePokemonDetail(id: Int): Flow<PokemonDetail?> = emptyFlow()

    override suspend fun refreshPokemonDetail(id: Int): Result<Unit> = Result.success(Unit)
}
