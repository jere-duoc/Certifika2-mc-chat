package com.certifika2.chat.service;
import com.certifika2.chat.dto.AnuncioEvent;
import com.certifika2.chat.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest @Import({NotificacionService.class, MensajeService.class})
class NotificacionServiceTest {
    @Autowired NotificacionService service;
    @Autowired NotificacionRepository notifications;
    @Autowired MensajeRepository messages;
    private AnuncioEvent evento(String id, List<String> recipients) {
        return new AnuncioEvent(1L, "curso-prueba", "Novedad", "Contenido", "prof", "Profesor",
                LocalDateTime.now(), id, "ANUNCIO", recipients);
    }
    @Test void fanoutIsPrivateAndDuplicateDeliveryIsIdempotent() {
        assertThat(service.recibir(evento("event-1", List.of("alice", "bob", "alice")), "sqs-1")).isTrue();
        assertThat(service.recibir(evento("event-1", List.of("alice", "bob")), "sqs-redelivery")).isFalse();
        assertThat(notifications.count()).isEqualTo(2);
        assertThat(messages.count()).isEqualTo(1);
        assertThat(service.listar("alice").noLeidas()).isEqualTo(1);
        assertThat(service.listar("outsider").items()).isEmpty();
        String id = service.listar("alice").items().get(0).id();
        assertThatThrownBy(() -> service.marcarLeida(id, "bob")).isInstanceOf(ResponseStatusException.class);
        service.marcarLeida(id, "alice");
        assertThat(service.listar("alice").noLeidas()).isZero();
        assertThat(service.listar("bob").noLeidas()).isEqualTo(1);
    }
    @Test void legacyMessagesKeepHistoryAndRetrySafely() {
        service.recibir(evento(null, null), "legacy-sqs-id");
        service.recibir(evento(null, null), "legacy-sqs-id");
        assertThat(messages.count()).isEqualTo(1);
        assertThat(notifications.count()).isZero();
    }
    @Test void announcementWithMaximumLengthFitsExistingHistory() {
        AnuncioEvent e = new AnuncioEvent(1L, "curso", "Titulo", "x".repeat(3000), "p", "Profesor",
                LocalDateTime.now(), "long", "ANUNCIO", List.of("alice"));
        service.recibir(e, "sqs");
        messages.flush(); notifications.flush();
        assertThat(service.listar("alice").items().get(0).contenido()).hasSize(3000);
    }
}
