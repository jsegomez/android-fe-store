package com.jsegomez.store.features.pokedex.presentation.sync

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jsegomez.store.R
import com.jsegomez.store.features.pokedex.domain.model.SyncState
import com.jsegomez.store.ui.theme.AppTextStyles
import org.koin.androidx.compose.koinViewModel
import java.text.DateFormat
import java.util.Date

@Composable
fun SyncPanel(modifier: Modifier = Modifier, viewModel: PokeSyncViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SyncPanelContent(
        uiState = uiState,
        onSync = viewModel::onSyncClick,
        onCancel = viewModel::onCancelClick,
        modifier = modifier,
    )
}

@Composable
private fun SyncPanelContent(
    uiState: SyncUiState,
    onSync: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Fecha formateada según el idioma del teléfono; solo se recalcula si cambia el valor
    val lastSuccess = remember(uiState.lastSuccessMillis) {
        uiState.lastSuccessMillis?.let { DateFormat.getDateTimeInstance().format(Date(it)) }
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.sync_status, stringResource(uiState.state.label())),
                style = AppTextStyles.BodyLargeSemiBold,
            )
            Text(
                text = stringResource(
                    R.string.sync_last_success,
                    lastSuccess ?: stringResource(R.string.sync_never),
                ),
                style = AppTextStyles.BodySmallMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Deshabilitado mientras hay un trabajo activo: KEEP ya evitaría duplicados,
                // pero así el usuario entiende que ya hay una actualización en marcha.
                Button(onClick = onSync, enabled = !uiState.isActive) {
                    Text(text = stringResource(R.string.sync_action_update))
                }
                OutlinedButton(onClick = onCancel, enabled = uiState.isActive) {
                    Text(text = stringResource(R.string.sync_action_cancel))
                }
            }
        }
    }
}

private fun SyncState.label(): Int = when (this) {
    SyncState.Idle -> R.string.sync_state_idle
    SyncState.Enqueued -> R.string.sync_state_enqueued
    SyncState.Running -> R.string.sync_state_running
    SyncState.Succeeded -> R.string.sync_state_succeeded
    SyncState.Failed -> R.string.sync_state_failed
    SyncState.Cancelled -> R.string.sync_state_cancelled
}
