package pokemonolith.util

import java.util.concurrent.CompletableFuture
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.startCoroutine
import kotlin.coroutines.suspendCoroutine

suspend fun <T> CompletableFuture<T>.await(): T = suspendCoroutine { cont ->
    this.handle { t, ex ->
        if (ex != null) {
            cont.resumeWithException(ex)
        } else {
            cont.resume(t)
        }
    }
}

fun runCoroutine(block: suspend () -> Unit) = block.startCoroutine(Continuation(EmptyCoroutineContext) {})
