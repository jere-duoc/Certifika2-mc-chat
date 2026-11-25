package com.certifika2.chat.config;
import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class AnuncioSqsConfig {
    @Bean
    public SqsMessageListenerContainerFactory<String> anunciosSqsFactory(SqsAsyncClient client) {
        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        // Ignore legacy producer's Java package type header; JSON is the actual wire contract.
        converter.setPayloadTypeMapper(message -> String.class);
        return SqsMessageListenerContainerFactory.<String>builder().sqsAsyncClient(client)
                .configure(options -> options.messageConverter(converter)).build();
    }
}
