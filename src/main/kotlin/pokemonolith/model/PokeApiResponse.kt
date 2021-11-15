package pokemonolith.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PokeApiResponse(
    val id: Int,
    val name: String,
    val moves: List<MoveWithDetails>,
    val stats: List<Stat>
)

data class MoveWithDetails(
    val move: Move,
    @JsonProperty("version_group_details")
    val versionGroupDetails: List<VersionGroupDetail>
)

data class Move(
    val name: String
)

data class VersionGroupDetail(
    @JsonProperty("level_learned_at")
    val levelLearnedAt: Int,
    @JsonProperty("move_learn_method")
    val moveLearnMethod: MoveLearnMethod,
    @JsonProperty("version_group")
    val versionGroup: VersionGroup
)

data class MoveLearnMethod(
    val name: String
)

data class VersionGroup(
    val name: String
)

data class Stat(
    @JsonProperty("base_stat")
    val baseStat: Int,
    val stat: StatType
)

data class StatType(
    val name: String
)