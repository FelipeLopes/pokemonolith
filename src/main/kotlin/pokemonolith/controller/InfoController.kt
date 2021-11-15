package pokemonolith.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import pokemonolith.service.PokeApiService

@Controller
class InfoController(
    private val pokeApiService: PokeApiService
) {
    @Get("/info/{pokemonName}")
    suspend fun info(@PathVariable pokemonName: String) = run {
        val pokemonModel = pokeApiService.getPokemonModel(pokemonName)
        pokemonModel?.let {
            HttpResponse.ok(it)
        } ?: HttpResponse.notFound("Pokemon not found")
    }
}