package com.certifika2.chat.listener;

import com.certifika2.chat.dto.AnuncioEvent;
import com.certifika2.chat.service.MensajeService;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnuncioSqsListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MensajeService mensajeService;

    @SqsListener("${app.sqs.queue-anuncios}")
    public void recibirAnuncio(AnuncioEvent anuncio) {
        log.info("Mensaje SQS de anuncio recibido para curso '{}' (ID: {}): {}",
                anuncio.codigoCurso(), anuncio.idCurso(), anuncio.titulo());

        String salaCurso = (anuncio.codigoCurso() != null && !anuncio.codigoCurso().isBlank())
                ? anuncio.codigoCurso()
                : String.valueOf(anuncio.idCurso());

        // 1. Guardar en base de datos para que los estudiantes desconectados puedan verlo en el historial
        try {
            String remitente = "📢 [ANUNCIO] " + (anuncio.nombreProfesor() != null ? anuncio.nombreProfesor() : "Profesor");
            String contenidoFormateado = "📌 " + anuncio.titulo() + "\n\n" + anuncio.contenido();
            mensajeService.guardarMensaje(
                    salaCurso,
                    anuncio.idProfesor(),
                    remitente,
                    contenidoFormateado
            );
            log.info("Anuncio registrado en el historial de chat de la sala '{}'", salaCurso);
        } catch (Exception ex) {
            log.warn("No fue posible persistir el anuncio en el historial del chat: {}", ex.getMessage());
        }

        // 2. Transmisión WebSocket en tiempo real hacia los suscriptores conectados
        String topicPrincipal = "/topic/curso/" + salaCurso;
        log.info("Difundiendo notificación en tiempo real a {}", topicPrincipal);
        messagingTemplate.convertAndSend(topicPrincipal, anuncio);

        // Si existe un identificador numérico adicional y es distinto, también notificar a dicho canal
        if (anuncio.idCurso() != null && !salaCurso.equals(String.valueOf(anuncio.idCurso()))) {
            String topicSecundario = "/topic/curso/" + anuncio.idCurso();
            messagingTemplate.convertAndSend(topicSecundario, anuncio);
        }
    }
}
