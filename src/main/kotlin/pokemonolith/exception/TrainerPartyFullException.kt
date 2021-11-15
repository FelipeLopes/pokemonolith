package pokemonolith.exception

class TrainerPartyFullException(id: String) : PokemonCreationException(id, "Trainer party is full")