package com.jsegomez.store.features.pokedex.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jsegomez.store.ui.components.BackButton

@Composable
fun PokemonDetailScreen(
    onBack: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(onClick = onBack, modifier = Modifier.padding(start = 4.dp, top = 8.dp))
        Text(text = "Pokemon Detail Screen")
    }
}