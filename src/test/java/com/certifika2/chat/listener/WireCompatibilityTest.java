package com.certifika2.chat.listener;
import io.awspring.cloud.sqs.support.converter.SqsMessagingMessageConverter;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class WireCompatibilityTest {
    @Test void legacyJavaTypeHeaderDoesNotRequireProducerClassInConsumer() {
        SqsMessagingMessageConverter converter = new SqsMessagingMessageConverter();
        converter.setPayloadTypeMapper(message -> String.class);
        Message message = Message.builder().messageId(java.util.UUID.randomUUID().toString()).receiptHandle("handle")
                .body("{\"idCurso\":1,\"titulo\":\"Legacy\"}")
                .messageAttributes(java.util.Map.of(SqsHeaders.SQS_DEFAULT_TYPE_HEADER,
                        MessageAttributeValue.builder().dataType("String")
                                .stringValue("com.certifika2.cursos.dto.AnuncioEvent").build())).build();
        assertThat(converter.toMessagingMessage(message).getPayload()).isEqualTo(message.body());
    }
}
