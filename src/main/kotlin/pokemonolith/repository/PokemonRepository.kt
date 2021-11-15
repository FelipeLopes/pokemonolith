package pokemonolith.repository

import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates.set
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import com.mongodb.reactivestreams.client.MongoClient
import jakarta.inject.Singleton
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import pokemonolith.entity.Pokemon
import pokemonolith.util.EntityStatus

@Singleton
class PokemonRepository(mongoClient: MongoClient) {
    private val pokemonCollection = mongoClient
        .getDatabase("pokemonolith")
        .getCollection("pokemon", Pokemon::class.java)

    suspend fun save(pokemon: Pokemon): InsertOneResult =
        pokemonCollection.insertOne(pokemon).awaitFirst()

    suspend fun findById(id: String) =
        pokemonCollection.find(
            eq("_id", id)
        ).awaitFirstOrNull()

    suspend fun confirmCreation(id: String): UpdateResult =
        pokemonCollection.updateOne(
            eq("_id", id),
            set("status", EntityStatus.CONFIRMED.name)
        ).awaitFirst()

    suspend fun errorOnCreation(id: String, msg: String): UpdateResult =
        pokemonCollection.updateOne(
            eq("_id", id),
            listOf(
                set("status", EntityStatus.ERROR.name),
                set("error", msg)
            )
        ).awaitFirst()
}