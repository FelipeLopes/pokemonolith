package pokemonolith.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import org.reactivestreams.Publisher
import pokemonolith.model.PokeApiResponse

@Client("https://pokeapi.co/api/v2")
interface PokeApiClient {
    @Get("/pokemon/{name}")
    fun pokemon(name: String): Publisher<PokeApiResponse>
}