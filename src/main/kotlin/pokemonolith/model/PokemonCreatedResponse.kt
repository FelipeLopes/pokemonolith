package pokemonolith.model

data class PokemonCreatedResponse(
    val id: String,
    val message: String = "Pokemon creation request received"
)
