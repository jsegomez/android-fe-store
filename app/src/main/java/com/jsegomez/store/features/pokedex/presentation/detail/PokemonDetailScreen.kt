package com.jsegomez.store.features.pokedex.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.jsegomez.store.R
import com.jsegomez.store.core.ui.ErrorState
import com.jsegomez.store.core.ui.LoadingState
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonStat
import com.jsegomez.store.ui.components.BackButton
import com.jsegomez.store.ui.theme.AppTextStyles
import org.koin.androidx.compose.koinViewModel
import java.util.Locale

private const val MAX_BASE_STAT = 255f

@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PokemonDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isFavorite by viewModel.isFavorite.collectAsStateWithLifecycle()

    PokemonDetailContent(
        uiState = uiState,
        isFavorite = isFavorite,
        onRetry = viewModel::load,
        onBack = onBack,
        onToggleFavorite = viewModel::onToggleFavorite,
        modifier = modifier,
    )
}

@Composable
private fun PokemonDetailContent(
    uiState: PokemonDetailUiState,
    isFavorite: Boolean,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            BackButton(onClick = onBack)
            if (uiState is PokemonDetailUiState.Success) {
                FavoriteButton(isFavorite = isFavorite, onClick = onToggleFavorite)
            }
        }

        when (uiState) {
            PokemonDetailUiState.Loading -> LoadingState()
            is PokemonDetailUiState.Error -> ErrorState(error = uiState.error, onRetry = onRetry)
            is PokemonDetailUiState.Success -> PokemonDetailBody(pokemon = uiState.pokemon)
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.pokemon_favorite_remove else R.string.pokemon_favorite_add
            ),
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun PokemonDetailBody(pokemon: PokemonDetail, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            model = pokemon.imageUrl,
            contentDescription = pokemon.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(220.dp),
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "#${pokemon.id.toString().padStart(3, '0')}",
                style = AppTextStyles.BodyMediumMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(text = pokemon.name.humanize(), style = AppTextStyles.HeadlineBold)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pokemon.types.forEach { type ->
                AssistChip(onClick = {}, label = { Text(text = type.humanize()) })
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            Measure(
                label = stringResource(R.string.pokemon_height),
                value = stringResource(R.string.pokemon_height_value, pokemon.heightMeters),
            )
            Measure(
                label = stringResource(R.string.pokemon_weight),
                value = stringResource(R.string.pokemon_weight_value, pokemon.weightKg),
            )
        }

        Section(title = stringResource(R.string.pokemon_abilities)) {
            pokemon.abilities.forEach { ability ->
                val hidden = if (ability.isHidden) " ${stringResource(R.string.pokemon_ability_hidden)}" else ""
                Text(text = ability.name.humanize() + hidden, style = AppTextStyles.BodyLargeRegular)
            }
        }

        Section(title = stringResource(R.string.pokemon_stats)) {
            pokemon.stats.forEach { StatRow(stat = it) }
        }
    }
}

@Composable
private fun Measure(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = AppTextStyles.BodyXLargeMedium)
        Text(
            text = label,
            style = AppTextStyles.BodySmallRegular,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = AppTextStyles.BodyLargeSemiBold)
        content()
    }
}

@Composable
private fun StatRow(stat: PokemonStat) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stat.name.humanize(),
            style = AppTextStyles.BodyMediumRegular,
            modifier = Modifier.width(120.dp),
        )
        Text(
            text = stat.value.toString(),
            style = AppTextStyles.BodyMediumSemiBold,
            modifier = Modifier.width(36.dp),
        )
        LinearProgressIndicator(
            progress = { (stat.value / MAX_BASE_STAT).coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(8.dp),
        )
    }
}

// "special-attack" -> "Special attack"
private fun String.humanize(): String =
    replace('-', ' ').replaceFirstChar { it.titlecase(Locale.getDefault()) }
