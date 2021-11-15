package pokemonolith.model

import pokemonolith.util.CreationStatus

data class ConfirmPokemonCreatedEvent(
    val id: String,
    val status: CreationStatus,
    val error: String? = null
)
