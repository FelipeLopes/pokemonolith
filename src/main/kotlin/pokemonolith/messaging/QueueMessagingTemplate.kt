package pokemonolith.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.inject.Singleton
import pokemonolith.util.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

@Singleton
class QueueMessagingTemplate(
    private val objectMapper: ObjectMapper,
    private val sqsAsyncClient: SqsAsyncClient)
{
    private val cache: MutableMap<String, String> = mutableMapOf()

    @Suppress("BlockingMethodInNonBlockingContext")
    suspend fun <T> sendMessage(queueName: String, data: T) = run {
        val queueUrl = getQueueUrlForName(queueName)
        val eventString = objectMapper.writeValueAsString(data)
        val messageBuilder = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(eventString)
        sqsAsyncClient.sendMessage(messageBuilder.build()).await()
    }

    private suspend fun getQueueUrlForName(name: String): String = run {
        cache[name] ?: sqsAsyncClient.getQueueUrl(
            GetQueueUrlRequest.builder().queueName(name).build()
        ).await().queueUrl().also {
            cache[name] = it
        }
    }
}