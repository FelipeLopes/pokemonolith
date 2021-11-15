package pokemonolith.controller

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.Post
import pokemonolith.entity.Trainer
import pokemonolith.model.TrainerCreatedResponse
import pokemonolith.model.TrainerRequest
import pokemonolith.repository.TrainerRepository

@Controller
class TrainerController(
    private val trainerRepository: TrainerRepository
) {
    @Post("/trainer")
    suspend fun createTrainer(
        @Body trainerRequest: TrainerRequest
    ) = run {
        val trainer = Trainer(
            name = trainerRequest.name,
            rival = trainerRequest.rival,
            money = trainerRequest.money
        )
        trainerRepository.save(trainer)
        HttpResponse.created(TrainerCreatedResponse(trainer.id))
    }

    @Get("/trainer/{id}")
    suspend fun getTrainer(
        @PathVariable id: String
    ) = trainerRepository.findById(id)?.let {
        HttpResponse.ok(it)
    } ?: HttpResponse.notFound("Trainer not found")

}