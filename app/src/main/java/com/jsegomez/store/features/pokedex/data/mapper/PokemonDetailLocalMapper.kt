package com.jsegomez.store.features.pokedex.data.mapper

import com.jsegomez.store.features.pokedex.data.local.CachedPokemonDetail
import com.jsegomez.store.features.pokedex.data.local.PokemonDetailEntity
import com.jsegomez.store.features.pokedex.data.local.PokemonEntity
import com.jsegomez.store.features.pokedex.data.local.PokemonStatEntity
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.domain.model.PokemonAbility
import com.jsegomez.store.features.pokedex.domain.model.PokemonDetail
import com.jsegomez.store.features.pokedex.domain.model.PokemonStat
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// Conversiones de la ficha de un Pokémon. Hay dos recorridos:
//   GUARDAR: respuesta de la API (DTO) -> filas de Room
//   LEER:    filas de Room             -> modelo de dominio que usa la pantalla

// ---------- GUARDAR ----------
// Se parte del DTO (y no del modelo de dominio) para guardar las medidas tal como las entrega
// la API, sin convertirlas a metros y luego deshacer la conversión.

fun PokemonDetailDto.toSummaryEntity(): PokemonEntity =
    PokemonEntity(id = id, name = name, imageUrl = artworkUrl(id))

// La fecha de descarga se recibe como parámetro (en vez de leer el reloj aquí dentro)
// para que quien llama decida el momento y la conversión siga siendo predecible.
fun PokemonDetailDto.toDetailEntity(lastUpdated: Long): PokemonDetailEntity =
    PokemonDetailEntity(
        pokemonId = id,
        heightDm = height,
        weightHg = weight,
        typesJson = Json.encodeToString(types.sortedBy { it.slot }.map { it.type.name }),
        abilitiesJson = Json.encodeToString(
            abilities.map { StoredAbility(name = it.ability.name, isHidden = it.isHidden) }
        ),
        lastUpdated = lastUpdated,
    )

fun PokemonDetailDto.toStatEntities(): List<PokemonStatEntity> =
    stats.mapIndexed { index, slot ->
        PokemonStatEntity(
            pokemonId = id,
            name = slot.stat.name,
            baseValue = slot.baseStat,
            position = index,
        )
    }

// ---------- LEER ----------

fun CachedPokemonDetail.toDomain(): PokemonDetail =
    PokemonDetail(
        id = pokemon.id,
        name = pokemon.name,
        imageUrl = pokemon.imageUrl,
        heightMeters = detail.heightDm / 10.0,
        weightKg = detail.weightHg / 10.0,
        types = Json.decodeFromString<List<String>>(detail.typesJson),
        abilities = Json.decodeFromString<List<StoredAbility>>(detail.abilitiesJson)
            .map { PokemonAbility(name = it.name, isHidden = it.isHidden) },
        // @Relation no garantiza el orden de las filas, así que se ordena por la posición guardada
        stats = stats.sortedBy { it.position }.map { PokemonStat(name = it.name, value = it.baseValue) },
    )

// Formato en el que se guardan las habilidades dentro de "abilitiesJson".
// Es privado a propósito: es un detalle de almacenamiento, no un modelo de la app.
@Serializable
private data class StoredAbility(val name: String, val isHidden: Boolean)
