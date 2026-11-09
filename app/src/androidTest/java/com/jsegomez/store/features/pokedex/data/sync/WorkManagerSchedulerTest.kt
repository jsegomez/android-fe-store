package com.jsegomez.store.features.pokedex.data.sync

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.NetworkType
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.jsegomez.store.features.pokedex.domain.model.SyncState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// WorkManager de prueba: ejecuta en un hilo controlado y NO cumple las restricciones por sí solo.
// Así el trabajo se queda en ENQUEUED (como "sin red") hasta que el test lo decida.
@RunWith(AndroidJUnit4::class)
class WorkManagerSchedulerTest {
    private lateinit var context: Context
    private lateinit var workManager: WorkManager
    private lateinit var scheduler: WorkManagerPokemonSyncScheduler

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder().setMinimumLoggingLevel(android.util.Log.DEBUG).build(),
        )
        workManager = WorkManager.getInstance(context)
        scheduler = WorkManagerPokemonSyncScheduler(context)
    }

    private fun workInfos() = workManager
        .getWorkInfosForUniqueWork(WorkManagerPokemonSyncScheduler.UNIQUE_WORK_NAME)
        .get()

    @Test
    fun repeatedRequests_keepASingleWork() {
        repeat(3) { scheduler.requestSync() }

        val infos = workInfos()
        assertEquals(1, infos.size)
        assertEquals(WorkInfo.State.ENQUEUED, infos.single().state)
    }

    @Test
    fun withoutNetwork_theWorkWaits_andIsReportedAsEnqueued() = runBlocking {
        scheduler.requestSync()

        assertEquals(SyncState.Enqueued, scheduler.observeSyncState().first())
    }

    @Test
    fun theRequestWaitsForANetworkConnection() {
        scheduler.requestSync()

        val constraints = workInfos().single().constraints
        assertEquals(NetworkType.CONNECTED, constraints.requiredNetworkType)
    }

    @Test
    fun cancel_endsTheWork_andIsNotReportedAsSuccess() = runBlocking {
        scheduler.requestSync()

        scheduler.cancelSync()

        assertEquals(SyncState.Cancelled, scheduler.observeSyncState().first())
    }

    @Test
    fun beforeAnyRequest_stateIsIdle() = runBlocking {
        assertEquals(SyncState.Idle, scheduler.observeSyncState().first())
    }
}
