package com.jsegomez.store.features.pokedex.presentation.sync

import com.jsegomez.store.features.pokedex.domain.PokemonSyncScheduler
import com.jsegomez.store.features.pokedex.domain.SyncMetadataStore
import com.jsegomez.store.features.pokedex.domain.model.SyncState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PokeSyncViewModelTest {
    private val scheduler = FakeScheduler()
    private val metadata = FakeMetadataStore()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // uiState usa WhileSubscribed: no observa nada hasta que alguien lo recoge.
    // Este suscriptor hace el papel de la pantalla.
    private fun TestScope.viewModelObservedByScreen(): PokeSyncViewModel {
        val viewModel = PokeSyncViewModel(scheduler, metadata)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        return viewModel
    }

    @Test
    fun uiState_startsWithNoWorkAndNoDate() = runTest {
        val state = viewModelObservedByScreen().uiState.value

        assertEquals(SyncState.Idle, state.state)
        assertNull(state.lastSuccessMillis)
    }

    @Test
    fun uiState_followsTheSchedulerState_andTheSavedDate() = runTest {
        val viewModel = viewModelObservedByScreen()

        scheduler.state.value = SyncState.Enqueued
        assertEquals(SyncState.Enqueued, viewModel.uiState.value.state)

        scheduler.state.value = SyncState.Succeeded
        metadata.lastSuccess.value = 1_700_000_000_000L
        val state = viewModel.uiState.value

        assertEquals(SyncState.Succeeded, state.state)
        assertEquals(1_700_000_000_000L, state.lastSuccessMillis)
    }

    @Test
    fun clicks_delegateToTheScheduler() = runTest {
        val viewModel = viewModelObservedByScreen()

        viewModel.onSyncClick()
        viewModel.onCancelClick()

        assertEquals(1, scheduler.requestCalls)
        assertEquals(1, scheduler.cancelCalls)
    }

    @Test
    fun isActive_isTrueOnlyWhileWaitingOrRunning() {
        assertTrue(SyncUiState(state = SyncState.Enqueued).isActive)
        assertTrue(SyncUiState(state = SyncState.Running).isActive)

        assertFalse(SyncUiState(state = SyncState.Idle).isActive)
        assertFalse(SyncUiState(state = SyncState.Succeeded).isActive)
        assertFalse(SyncUiState(state = SyncState.Failed).isActive)
        assertFalse(SyncUiState(state = SyncState.Cancelled).isActive)
    }
}

private class FakeScheduler : PokemonSyncScheduler {
    val state = MutableStateFlow<SyncState>(SyncState.Idle)
    var requestCalls = 0
    var cancelCalls = 0

    override fun requestSync() {
        requestCalls++
    }

    override fun cancelSync() {
        cancelCalls++
    }

    override fun observeSyncState(): Flow<SyncState> = state
}

private class FakeMetadataStore : SyncMetadataStore {
    val lastSuccess = MutableStateFlow<Long?>(null)

    override fun observeLastSuccess(): Flow<Long?> = lastSuccess

    override suspend fun markSuccess(timeMillis: Long) {
        lastSuccess.value = timeMillis
    }
}
