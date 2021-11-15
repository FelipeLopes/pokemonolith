package pokemonolith.model

data class PokemonModel(
    val id: Int,
    val name: String,
    val baseHp: Int,
    val baseAttack: Int,
    val baseDefense: Int,
    val baseSpeed: Int,
    val baseSpecial: Int,
    val movePool: List<MoveModel>
)

data class MoveModel(
    val name: String,
    val learnMethod: String,
    val levelLearnedAt: Int
)
