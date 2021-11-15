package pokemonolith.listener

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import kotlinx.coroutines.delay
import pokemonolith.entity.TrainerPokemon
import pokemonolith.exception.PokemonCreationException
import pokemonolith.exception.TrainerDoesNotExistException
import pokemonolith.exception.TrainerPartyFullException
import pokemonolith.messaging.QueueMessagingTemplate
import pokemonolith.messaging.SqsAcknowledgment
import pokemonolith.model.ConfirmPokemonCreatedEvent
import pokemonolith.model.PokemonCreatedEvent
import pokemonolith.repository.TrainerRepository
import pokemonolith.util.CreationStatus

@Singleton
class PokemonCreatedListener(
    private val objectMapper: ObjectMapper,
    private val trainerRepository: TrainerRepository,
    private val queueMessagingTemplate: QueueMessagingTemplate
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun consumeEvent(eventString: String, acknowledgment: SqsAcknowledgment) = run {
        try {
            val event = objectMapper.readValue(eventString, PokemonCreatedEvent::class.java)
            val trainerId = event.trainerId
            val trainerPokemon = TrainerPokemon(
                id = event.id,
                species = event.species,
                nickname = event.nickname
            )
            val trainer = trainerRepository.findById(trainerId) ?: throw TrainerDoesNotExistException(event.id)
            if (trainer.party.size >= 6) {
                throw TrainerPartyFullException(event.id)
            }
            trainerRepository.addPokemonToParty(trainerId, trainerPokemon)
            queueMessagingTemplate.sendMessage("confirm-pokemon-created", ConfirmPokemonCreatedEvent(
                status = CreationStatus.CONFIRMED,
                id = event.id
            ))
        } catch (ex: PokemonCreationException) {
            queueMessagingTemplate.sendMessage("confirm-pokemon-created", ConfirmPokemonCreatedEvent(
                status = CreationStatus.ERROR,
                id = ex.id,
                error = ex.message
            ))
        } catch (t: Throwable) {
            println("Got exception: ${t.message}")
        } finally {
            acknowledgment.acknowledge()
        }
    }
}