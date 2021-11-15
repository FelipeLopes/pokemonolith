package pokemonolith.service

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitFirst
import pokemonolith.client.PokeApiClient
import pokemonolith.model.MoveModel
import pokemonolith.model.PokemonModel

@Singleton
class PokeApiService(
    private val pokeApiClient: PokeApiClient
) {
    suspend fun getPokemonModel(name: String): PokemonModel? {
        if (name.toIntOrNull() != null) {
            return null
        }
        try {
            val response = pokeApiClient.pokemon(name).awaitFirst()
            if (response.id > 151) {
                return null
            }
            val movePool = response.moves.flatMap { move ->
                move.versionGroupDetails.firstOrNull { it.versionGroup.name == "red-blue" }?.let {
                    listOf(
                        MoveModel(
                            name = move.move.name,
                            learnMethod = it.moveLearnMethod.name,
                            levelLearnedAt = it.levelLearnedAt
                        )
                    )
                } ?: listOf()
            }
            return PokemonModel(
                id = response.id,
                name = response.name,
                baseHp = response.stats.first { it.stat.name == "hp" }.baseStat,
                baseAttack = response.stats.first { it.stat.name == "attack" }.baseStat,
                baseDefense = response.stats.first { it.stat.name == "defense" }.baseStat,
                baseSpeed = response.stats.first { it.stat.name == "speed" }.baseStat,
                baseSpecial = response.stats.first { it.stat.name == "special-attack" }.baseStat,
                movePool = movePool
            )
        } catch (ex: HttpClientResponseException) {
            if (ex.status == HttpStatus.NOT_FOUND) {
                return null
            } else {
                throw ex
            }
        }
    }
}