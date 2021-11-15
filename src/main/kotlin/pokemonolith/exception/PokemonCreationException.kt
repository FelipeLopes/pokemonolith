package pokemonolith.exception

import java.lang.Exception

open class PokemonCreationException(val id: String, message: String) : Exception(message)