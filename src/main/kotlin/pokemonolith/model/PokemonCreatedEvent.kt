package pokemonolith.model

data class PokemonCreatedEvent(
    val trainerId: String,
    val id: String,
    val species: String,
    val nickname: String
)