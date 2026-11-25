package com.certifika2.chat.listener;
import com.certifika2.chat.service.NotificacionService;
import com.certifika2.chat.dto.AnuncioEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import software.amazon.awssdk.services.sqs.model.Message;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AnuncioSqsListenerTest {
    @Test void publicBroadcastDoesNotExposeRecipientIds() throws Exception {
        NotificacionService service = mock(NotificacionService.class);
        when(service.recibir(any(), any())).thenReturn(true);
        SimpMessagingTemplate websocket = mock(SimpMessagingTemplate.class);
        AnuncioSqsListener listener = new AnuncioSqsListener(new ObjectMapper().findAndRegisterModules(), service, websocket);
        listener.recibirAnuncio("{\"idCurso\":1,\"codigoCurso\":\"curso\",\"destinatarios\":[\"private-student\"]}",
                Message.builder().messageId("message").build());
        org.mockito.ArgumentCaptor<AnuncioEvent> payload = org.mockito.ArgumentCaptor.forClass(AnuncioEvent.class);
        verify(websocket).convertAndSend(eq("/topic/curso/curso"), payload.capture());
        assertThat(payload.getValue().destinatarios()).isNull();
    }
    @Test void databaseFailurePropagatesSoSqsDoesNotAcknowledge() throws Exception {
        NotificacionService service = mock(NotificacionService.class);
        doThrow(new IllegalStateException("DB unavailable")).when(service).recibir(any(AnuncioEvent.class), eq("message"));
        SimpMessagingTemplate websocket = mock(SimpMessagingTemplate.class);
        AnuncioSqsListener listener = new AnuncioSqsListener(new ObjectMapper().findAndRegisterModules(), service, websocket);
        assertThatThrownBy(() -> listener.recibirAnuncio("{\"idCurso\":1,\"titulo\":\"Test\"}",
                Message.builder().messageId("message").build())).isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(websocket);
    }
}
