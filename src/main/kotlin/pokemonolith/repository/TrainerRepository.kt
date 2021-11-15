package pokemonolith.repository

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.reactivestreams.client.MongoClient
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import pokemonolith.entity.Trainer
import pokemonolith.entity.TrainerPokemon

@Singleton
class TrainerRepository(mongoClient: MongoClient) {
    private val trainerCollection = mongoClient
        .getDatabase("pokemonolith")
        .getCollection("trainer", Trainer::class.java)

    suspend fun save(trainer: Trainer) =
        trainerCollection.insertOne(trainer).awaitFirst()

    suspend fun findById(id: String) =
        trainerCollection.find(
            Filters.eq("_id", id)
        ).awaitFirstOrNull()

    suspend fun addPokemonToParty(id: String, pokemon: TrainerPokemon) =
        trainerCollection.updateOne(
            Filters.eq("_id", id),
            Updates.addToSet("party", pokemon)
        ).awaitFirstOrNull()
}