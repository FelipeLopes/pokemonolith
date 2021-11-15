package pokemonolith.model

data class PokemonRequest(
    val species: String,
    val nickname: String,
    val level: Int,
    val moves: List<String>,
    val diversityValues: List<Int>,
    val statExp: List<Int>
)