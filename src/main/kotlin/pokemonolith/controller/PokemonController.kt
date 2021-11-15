package pokemonolith.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import pokemonolith.entity.Pokemon
import pokemonolith.entity.TrainerPokemon
import pokemonolith.messaging.QueueMessagingTemplate
import pokemonolith.model.PokemonCreatedEvent
import pokemonolith.model.PokemonCreatedResponse
import pokemonolith.model.PokemonErrorResponse
import pokemonolith.model.PokemonRequest
import pokemonolith.repository.PokemonRepository
import pokemonolith.repository.TrainerRepository
import pokemonolith.service.PokeApiService
import pokemonolith.util.CreationStatus
import pokemonolith.util.EntityStatus
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import kotlin.math.floor
import kotlin.math.sqrt

@Controller
class PokemonController(
    private val pokeApiService: PokeApiService,
    private val pokemonRepository: PokemonRepository,
    private val trainerRepository: TrainerRepository,
    private val queueMessagingTemplate: QueueMessagingTemplate
) {
    @Post("/pokemon/{trainerId}")
    suspend fun createPokemon(
        @PathVariable trainerId: String,
        @Body pokemonRequest: PokemonRequest
    ): HttpResponse<Any> {
        if (pokemonRequest.level < 1 || pokemonRequest.level > 100) {
            return HttpResponse.badRequest("level should be between 1 and 100")
        }
        if (pokemonRequest.moves.size > 4) {
            return HttpResponse.badRequest("moves should not be more than 4")
        }
        if (pokemonRequest.moves.size != pokemonRequest.moves.distinct().size) {
            return HttpResponse.badRequest("moves should all be unique")
        }
        if (pokemonRequest.diversityValues.size != 4) {
            return HttpResponse.badRequest("diversityValues should be an array of size 4")
        }
        if (pokemonRequest.diversityValues.any { it < 0 || it > 15 }) {
            return HttpResponse.badRequest("All diversityValues should be between 0 and 15")
        }
        if (pokemonRequest.statExp.size != 5) {
            return HttpResponse.badRequest("statExp should be an array of size 5")
        }
        if (pokemonRequest.statExp.any { it < 0 || it > 65535 }) {
            return HttpResponse.badRequest("All statExp values should be between 0 and 65535")
        }
        val pokemonModel = pokeApiService.getPokemonModel(pokemonRequest.species)
            ?: return HttpResponse.badRequest("Pokemon species must be legal")
        val illegalMove = pokemonRequest.moves.filter { move ->
            val poolMove = pokemonModel.movePool.firstOrNull { it.name == move }
            poolMove == null || (poolMove.learnMethod == "level-up" &&
                    poolMove.levelLearnedAt > pokemonRequest.level)
        }.firstOrNull()
        if (illegalMove != null) {
            return HttpResponse.badRequest("$illegalMove is an illegal move for " +
                    "L${pokemonRequest.level} ${pokemonRequest.species}")
        }

        val attackDV = pokemonRequest.diversityValues[0]
        val defenseDV = pokemonRequest.diversityValues[1]
        val speedDV = pokemonRequest.diversityValues[2]
        val specialDV = pokemonRequest.diversityValues[3]

        val hpDV = (attackDV % 2)*8 + (defenseDV % 2)*4 + (speedDV % 2)*2 + specialDV % 2

        val hp = calculateHpStat(pokemonModel.baseHp, hpDV, pokemonRequest.statExp[0], pokemonRequest.level)
        val attack = calculateStat(pokemonModel.baseAttack, attackDV, pokemonRequest.statExp[1], pokemonRequest.level)
        val defense = calculateStat(pokemonModel.baseDefense, defenseDV, pokemonRequest.statExp[2], pokemonRequest.level)
        val speed = calculateStat(pokemonModel.baseSpeed, speedDV, pokemonRequest.statExp[3], pokemonRequest.level)
        val special = calculateStat(pokemonModel.baseSpecial, specialDV, pokemonRequest.statExp[4], pokemonRequest.level)

        val pokemon = Pokemon(
            species = pokemonRequest.species,
            nickname = pokemonRequest.nickname,
            hp = hp,
            attack = attack,
            defense = defense,
            speed = speed,
            special = special,
            originalTrainerId = trainerId
        )
        pokemonRepository.save(pokemon)
        val pokemonCreatedEvent = PokemonCreatedEvent(
            trainerId = trainerId,
            id = pokemon.id,
            species = pokemon.species,
            nickname = pokemon.nickname
        )
        queueMessagingTemplate.sendMessage("pokemon-created", pokemonCreatedEvent)
        return HttpResponse.accepted<PokemonCreatedResponse>().body(PokemonCreatedResponse(pokemon.id))
    }

    @Get("/pokemon/{id}")
    suspend fun getPokemon(id: String) =
        pokemonRepository.findById(id)?.let { pokemon ->
            when (pokemon.status) {
                EntityStatus.PENDING ->
                    HttpResponse.accepted<PokemonCreatedResponse>().body(PokemonCreatedResponse(pokemon.id))
                EntityStatus.ERROR ->
                    HttpResponse.unprocessableEntity<PokemonErrorResponse>().body(
                        PokemonErrorResponse(pokemon.error ?: "Unknown error")
                    )
                EntityStatus.CONFIRMED ->
                    HttpResponse.ok(pokemon)
            }
        } ?: HttpResponse.notFound("Pokemon not found")

    private fun calculateHpStat(base: Int, dv: Int, statExp: Int, level: Int) =
        floor(((base+dv)*2.0+floor((sqrt(statExp.toDouble())+1)/4.0))*(level/100.0)).toInt()+level+10

    private fun calculateStat(base: Int, dv: Int, statExp: Int, level: Int) =
        floor(((base+dv)*2.0+floor((sqrt(statExp.toDouble())+1)/4.0))*(level/100.0)).toInt()+5
}