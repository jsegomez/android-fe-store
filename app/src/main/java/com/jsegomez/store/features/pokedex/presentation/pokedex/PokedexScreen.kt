package com.jsegomez.store.features.pokedex.presentation.pokedex

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.jsegomez.store.R
import com.jsegomez.store.core.network.AppError
import com.jsegomez.store.core.ui.toMessage
import com.jsegomez.store.features.pokedex.domain.model.PokemonSummary
import com.jsegomez.store.ui.components.BackButton
import com.jsegomez.store.ui.theme.AppTextStyles

@Composable
fun PokedexScreen(
    modifier: Modifier = Modifier,
    viewModel: PokedexViewModel = viewModel(),
    onBack: () -> Unit,
    onPokemonClick: (Int) -> Unit,
){
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    PokedexContent(
        uiState = uiState,
        onRetry = viewModel::loadPokemon,
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

        when {
            uiState.pokemon.isNotEmpty() -> PokemonGrid(pokemon = uiState.pokemon, onPokemonClick = onPokemonClick)
            uiState.isLoading -> LoadingState()
            uiState.error != null -> ErrorState(error = uiState.error, onRetry = onRetry)
        }
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

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(error: AppError, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = error.toMessage(), style = AppTextStyles.BodyLargeSemiBold, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(text = stringResource(R.string.action_retry)) }
    }
}