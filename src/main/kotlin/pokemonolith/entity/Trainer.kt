package pokemonolith.entity

import java.util.*

data class Trainer(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var rival: String = "",
    var money: Int = 0,
    var party: List<TrainerPokemon> = emptyList()
)

data class TrainerPokemon(
    var id: String = "",
    var species: String = "",
    var nickname: String = ""
)
