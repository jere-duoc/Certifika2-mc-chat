package com.certifika2.chat.listener;
import com.certifika2.chat.dto.AnuncioEvent;
import com.certifika2.chat.service.NotificacionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import io.awspring.cloud.sqs.listener.SqsHeaders;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;

@Component @RequiredArgsConstructor @Slf4j
public class AnuncioSqsListener {
    private final ObjectMapper mapper;
    private final NotificacionService service;
    private final SimpMessagingTemplate messagingTemplate;

    @SqsListener(value = "${app.sqs.queue-anuncios}", factory = "anunciosSqsFactory", acknowledgementMode = "ON_SUCCESS")
    public void recibirAnuncio(String json, @Header(SqsHeaders.SQS_SOURCE_DATA_HEADER) Message original) throws Exception {
        AnuncioEvent evento = mapper.readValue(json, AnuncioEvent.class);
        // Exceptions must reach SQS: acknowledge only after durable history + individual inbox commit.
        if (!service.recibir(evento, original.messageId())) return;
        // A course-wide websocket event must never expose the recipient snapshot.
        AnuncioEvent publico = new AnuncioEvent(evento.idCurso(), evento.codigoCurso(), evento.titulo(),
                evento.contenido(), evento.idProfesor(), evento.nombreProfesor(), evento.fechaCreacion(),
                evento.eventoId(), evento.tipoEvento(), null);
        try {
            String sala = evento.codigoCurso() != null ? evento.codigoCurso() : String.valueOf(evento.idCurso());
            messagingTemplate.convertAndSend("/topic/curso/" + sala, publico);
            if (evento.idCurso() != null && !sala.equals(String.valueOf(evento.idCurso()))) {
                messagingTemplate.convertAndSend("/topic/curso/" + evento.idCurso(), publico);
            }
        } catch (RuntimeException ex) {
            log.warn("Difusión websocket no disponible; las notificaciones ya están guardadas");
        }
    }
}
