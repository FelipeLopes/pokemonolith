package pokemonolith.messaging

import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import java.net.URI

@Factory
class AwsFactory {
    @Singleton
    fun sqsAsyncClient() =
        SqsAsyncClient.builder()
            .endpointOverride(URI.create("http://localhost:4566"))
            .region(Region.of("sa-east-1"))
            .credentialsProvider(StaticCredentialsProvider.create(object : AwsCredentials {
                override fun accessKeyId() = "FAKE"
                override fun secretAccessKey() = "FAKE"
            })).build()
}