package pokemonolith.messaging

import pokemonolith.util.await
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse

class SqsAcknowledgment(
    private val sqsAsyncClient: SqsAsyncClient,
    private val queueUrl: String,
    private val receiptHandle: String
) {
    suspend fun acknowledge(): DeleteMessageResponse =
        sqsAsyncClient.deleteMessage(
            DeleteMessageRequest.builder().queueUrl(queueUrl)
                .receiptHandle(receiptHandle).build()
        ).await()
}