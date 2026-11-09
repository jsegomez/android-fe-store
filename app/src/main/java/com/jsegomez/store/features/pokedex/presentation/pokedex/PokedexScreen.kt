package com.jsegomez.store.features.pokedex.presentation.pokedex

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jsegomez.store.R
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.core.ui.ErrorState
import com.jsegomez.store.core.ui.LoadingState
import com.jsegomez.store.core.ui.toMessage
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import com.jsegomez.store.ui.components.BackButton
import com.jsegomez.store.ui.theme.AppTextStyles
import org.koin.androidx.compose.koinViewModel

@Composable
fun PokedexScreen(
    modifier: Modifier = Modifier,
    viewModel: PokedexViewModel = koinViewModel(),
    onBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PokedexContent(
        uiState = uiState,
        onRetry = viewModel::retry,
        onBack = onBack,
        onPokemonClick = onPokemonClick,
        modifier = modifier,
    )
}

@Composable
private fun PokedexContent(
    uiState: PokedexUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        BackButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 8.dp))

        // El ORDEN de las ramas importa: lo guardado va primero, así un error de red
        // nunca esconde una lista que sí tenemos.
        when {
            uiState.pokemon.isNotEmpty() -> {
                if (uiState.isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                uiState.refreshError?.let { RefreshErrorBanner(error = it, onRetry = onRetry) }
                PokemonGrid(pokemon = uiState.pokemon, onPokemonClick = onPokemonClick)
            }
            // Sin datos guardados: o falló leer el disco, o estamos esperando la primera descarga
            uiState.error != null -> ErrorState(error = uiState.error, onRetry = onRetry)
            uiState.isLoading || uiState.isRefreshing -> LoadingState()
            // Sin datos y la descarga falló: no hay nada que mostrar, solo explicar y permitir reintentar
            uiState.refreshError != null -> ErrorState(error = uiState.refreshError, onRetry = onRetry)
        }
    }
}

// Aviso que se muestra ENCIMA de la lista guardada cuando no se pudo actualizar
@Composable
private fun RefreshErrorBanner(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = error.toMessage(),
                style = AppTextStyles.BodyMediumSemiBold,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(R.string.pokedex_refresh_failed),
                style = AppTextStyles.BodySmallRegular,
                color = MaterialTheme.colorScheme.onErrorContainer,
            )
        }
        TextButton(onClick = onRetry) { Text(text = stringResource(R.string.action_retry)) }
    }
}

@Composable
private fun PokemonGrid(pokemon: List<PokemonSummary>, onPokemonClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize(),
    ) {
        items(items = pokemon, key = { it.id }) { PokemonCard(pokemon = it, onPokemonClick = onPokemonClick) }
    }
}

@Composable
private fun PokemonCard(pokemon: PokemonSummary, onPokemonClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth(),
        onClick = { onPokemonClick(pokemon.id) }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                AsyncImage(
                    model = pokemon.imageUrl,
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(110.dp),
                )
                Text(
                    text = "#${pokemon.id.toString().padStart(3, '0')}",
                    style = AppTextStyles.BodySmallMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = pokemon.name.replaceFirstChar { it.uppercase() },
                    style = AppTextStyles.BodyLargeSemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
