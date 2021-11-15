package pokemonolith

import io.micronaut.runtime.Micronaut.*

fun main(args: Array<String>) {
	build()
	    .args(*args)
		.packages("pokemonolith")
		.start()
}

