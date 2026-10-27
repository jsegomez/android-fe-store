package com.jsegomez.store.features.pokedex.domain.model

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val heightMeters: Double,
    val weightKg: Double,
    val types: List<String>,
    val abilities: List<PokemonAbility>,
    val stats: List<PokemonStat>,
)

data class PokemonAbility(val name: String, val isHidden: Boolean)
data class PokemonStat(val name: String, val value: Int)