package com.certifika2.chat.controller;

import com.certifika2.chat.dto.AnuncioEvent;
import com.certifika2.chat.repository.*;
import com.certifika2.chat.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.List;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import({NotificacionService.class, MensajeService.class, DeliveryAdminController.class})
class DeliveryAdminControllerTest {
    @Autowired DeliveryAdminController controller;
    @Autowired NotificacionService service;

    @Test void reportsCommittedDeliveryAndReadCountsWithoutInventingReceipts() {
        service.recibir(new AnuncioEvent(1L, "curso", "Aviso", "Contenido", "prof", "Profesor",
                LocalDateTime.now(), "event-1", "ANUNCIO", List.of("alice", "bob", "alice")), "sqs-1");
        service.marcarLeida(service.listar("alice").items().get(0).id(), "alice");
        var events = controller.lookup(List.of("event-1", "unknown", "event-1")).events();
        assertThat(events).hasSize(2);
        assertThat(events.get(0).processed()).isTrue();
        assertThat(events.get(0).delivered()).isEqualTo(2);
        assertThat(events.get(0).readCount()).isEqualTo(1);
        assertThat(events.get(1).processed()).isFalse();
        assertThat(events.get(1).delivered()).isZero();
    }
    @Test void processedEventWithNoRecipientsIsStillConfirmed() {
        service.recibir(new AnuncioEvent(1L, "curso", "Aviso", "C", "p", "Profesor",
                LocalDateTime.now(), "empty", "ANUNCIO", List.of()), "sqs-empty");
        var receipt = controller.lookup(List.of("empty")).events().get(0);
        assertThat(receipt.processed()).isTrue();
        assertThat(receipt.delivered()).isZero();
    }
    @Test void rejectsUnboundedOrInvalidLookups() {
        assertThatThrownBy(() -> controller.lookup(java.util.Collections.nCopies(201, "id"))).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> controller.lookup(List.of(" "))).isInstanceOf(ResponseStatusException.class);
        assertThat(controller.lookup(List.of()).events()).isEmpty();
    }
}
