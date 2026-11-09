package com.jsegomez.store.features.pokedex.data.mapper

import com.jsegomez.store.features.pokedex.data.local.CachedPokemonDetail
import com.jsegomez.store.features.pokedex.data.remote.dto.AbilitySlotDto
import com.jsegomez.store.features.pokedex.data.remote.dto.NamedResourceDto
import com.jsegomez.store.features.pokedex.data.remote.dto.PokemonDetailDto
import com.jsegomez.store.features.pokedex.data.remote.dto.SpritesDto
import com.jsegomez.store.features.pokedex.data.remote.dto.StatSlotDto
import com.jsegomez.store.features.pokedex.data.remote.dto.TypeSlotDto
import com.jsegomez.store.features.pokedex.domain.model.PokemonAbility
import org.junit.Assert.assertEquals
import org.junit.Test

class PokemonDetailLocalMapperTest {

    // Recorrido completo: lo que la API entrega -> filas de Room -> modelo de dominio.
    // Las estadísticas se barajan antes de leer, porque Room no garantiza el orden de las filas.
    private fun roundTrip(dto: PokemonDetailDto) = CachedPokemonDetail(
        detail = dto.toDetailEntity(lastUpdated = DOWNLOADED_AT),
        pokemon = dto.toSummaryEntity(),
        stats = dto.toStatEntities().reversed(),
    ).toDomain()

    @Test
    fun roundTrip_keepsTheStatsInTheOrderTheApiGaveThem() {
        val domain = roundTrip(bulbasaur())

        assertEquals(listOf("hp", "attack", "defense"), domain.stats.map { it.name })
        assertEquals(listOf(45, 49, 49), domain.stats.map { it.value })
    }

    @Test
    fun roundTrip_ordersTypesBySlot() {
        // La API puede entregar los tipos desordenados; el slot indica cuál es el principal
        val domain = roundTrip(bulbasaur())

        assertEquals(listOf("grass", "poison"), domain.types)
    }

    @Test
    fun roundTrip_keepsTheHiddenFlagOfEachAbility() {
        val domain = roundTrip(bulbasaur())

        assertEquals(
            listOf(PokemonAbility("overgrow", isHidden = false), PokemonAbility("chlorophyll", isHidden = true)),
            domain.abilities,
        )
    }

    @Test
    fun roundTrip_convertsApiUnitsToMetersAndKilograms() {
        val domain = roundTrip(bulbasaur())

        assertEquals(0.7, domain.heightMeters, 0.0001)
        assertEquals(6.9, domain.weightKg, 0.0001)
    }

    @Test
    fun detailEntity_storesTheDownloadDateItWasGiven() {
        assertEquals(DOWNLOADED_AT, bulbasaur().toDetailEntity(lastUpdated = DOWNLOADED_AT).lastUpdated)
    }

    @Test
    fun summaryEntity_usesTheSameImageUrlAsTheList() {
        val summary = bulbasaur().toSummaryEntity()

        assertEquals(1, summary.id)
        assertEquals("bulbasaur", summary.name)
        assertEquals(
            "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png",
            summary.imageUrl,
        )
    }

    private fun bulbasaur() = PokemonDetailDto(
        id = 1,
        name = "bulbasaur",
        height = 7,
        weight = 69,
        baseExperience = 64,
        // Desordenados a propósito: poison (slot 2) llega antes que grass (slot 1)
        types = listOf(TypeSlotDto(slot = 2, type = named("poison")), TypeSlotDto(slot = 1, type = named("grass"))),
        abilities = listOf(
            AbilitySlotDto(ability = named("overgrow"), isHidden = false, slot = 1),
            AbilitySlotDto(ability = named("chlorophyll"), isHidden = true, slot = 3),
        ),
        stats = listOf(
            StatSlotDto(baseStat = 45, effort = 0, stat = named("hp")),
            StatSlotDto(baseStat = 49, effort = 0, stat = named("attack")),
            StatSlotDto(baseStat = 49, effort = 0, stat = named("defense")),
        ),
        sprites = SpritesDto(),
    )

    private fun named(name: String) = NamedResourceDto(name = name, url = "https://pokeapi.co/api/v2/x/$name/")

    private companion object {
        const val DOWNLOADED_AT = 1_700_000_000_000L
    }
}
