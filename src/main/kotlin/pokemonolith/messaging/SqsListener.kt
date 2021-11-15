package pokemonolith.messaging

import io.micronaut.context.annotation.Context
import kotlinx.coroutines.runBlocking
import pokemonolith.listener.ConfirmPokemonCreatedListener
import pokemonolith.listener.PokemonCreatedListener
import pokemonolith.util.await
import pokemonolith.util.runCoroutine
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import software.amazon.awssdk.services.sqs.model.SqsException
import javax.annotation.PostConstruct

@Context
class SqsListener(
    private val sqsAsyncClient: SqsAsyncClient,
    private val pokemonCreatedListener: PokemonCreatedListener,
    private val confirmPokemonCreatedListener: ConfirmPokemonCreatedListener
) {

    companion object {
        const val NUMBER_OF_MESSAGES = 10
        const val WAIT_TIME_SECONDS = 10
        const val VISIBILITY_TIMEOUT = 30
    }

    class QueueConsumer(
        val queueName: String,
        val callback: suspend (String, SqsAcknowledgment) -> Unit
    )

    private suspend fun fetchQueueUrl(queueName: String) = run {
        sqsAsyncClient.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build())
            .await().queueUrl()
    }

    private fun registerListener(queueUrl: String, consumer: suspend (String, SqsAcknowledgment) -> Unit) = run {
        val receiveMessageMono = Mono.fromFuture {
            sqsAsyncClient.receiveMessage(
                ReceiveMessageRequest.builder()
                    .maxNumberOfMessages(NUMBER_OF_MESSAGES)
                    .queueUrl(queueUrl)
                    .waitTimeSeconds(WAIT_TIME_SECONDS)
                    .visibilityTimeout(VISIBILITY_TIMEOUT)
                    .build()
            )
        }

        receiveMessageMono
            .repeat()
            .retry()
            .flatMap { Flux.fromIterable(it.messages()) }
            .subscribe { message ->
                runCoroutine {
                    val acknowledgment = SqsAcknowledgment(sqsAsyncClient, queueUrl, message.receiptHandle())
                    val eventString = message.body()
                    try {
                        consumer(eventString, acknowledgment)
                    } catch (t: Throwable) {
                        println("Received exception: ${t.message}")
                    }
                }
            }
    }

    @PostConstruct
    fun initListener() = runBlocking {
        val queueConsumers = listOf(
            QueueConsumer("pokemon-created") { event, ack ->
                pokemonCreatedListener.consumeEvent(event, ack)
            },
            QueueConsumer("confirm-pokemon-created") { event, ack ->
                confirmPokemonCreatedListener.consumeEvent(event, ack)
            }
        )
        for (consumer in queueConsumers) {
            try {
                val queueUrl = fetchQueueUrl(consumer.queueName)
                registerListener(queueUrl, consumer.callback)
            } catch (ex: SqsException) {
                println("Could not get queue URL for name ${consumer.queueName}. Error: ${ex.message}")
            }
        }
        println("Successfully initialized SQS Listener.")
    }
}