package pokemonolith.listener

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import pokemonolith.messaging.SqsAcknowledgment
import pokemonolith.model.ConfirmPokemonCreatedEvent
import pokemonolith.repository.PokemonRepository
import pokemonolith.util.CreationStatus
import pokemonolith.util.EntityStatus

@Singleton
class ConfirmPokemonCreatedListener(
    private val objectMapper: ObjectMapper,
    private val pokemonRepository: PokemonRepository
) {
    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun consumeEvent(eventString: String, acknowledgment: SqsAcknowledgment) = run {
        try {
            val event = objectMapper.readValue(eventString, ConfirmPokemonCreatedEvent::class.java)
            when (event.status) {
                CreationStatus.CONFIRMED -> pokemonRepository.confirmCreation(event.id)
                CreationStatus.ERROR -> pokemonRepository.errorOnCreation(event.id, event.error!!)
            }
        } catch (t: Throwable) {
            println("Got exception: ${t.message}")
        }
        acknowledgment.acknowledge()
    }
}