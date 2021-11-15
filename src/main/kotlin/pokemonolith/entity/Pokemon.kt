package pokemonolith.entity

import pokemonolith.util.EntityStatus
import java.util.*

data class Pokemon(
    var id: String = UUID.randomUUID().toString(),
    var species: String = "",
    var nickname: String = "",
    var hp: Int = 0,
    var attack: Int = 0,
    var defense: Int = 0,
    var speed: Int = 0,
    var special: Int = 0,
    var originalTrainerId: String = "",
    var status: EntityStatus = EntityStatus.PENDING,
    var error: String? = null
)