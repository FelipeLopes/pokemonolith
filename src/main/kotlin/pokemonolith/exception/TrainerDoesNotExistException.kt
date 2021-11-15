package pokemonolith.exception

class TrainerDoesNotExistException(id: String) : PokemonCreationException(id, "Trainer does not exist")