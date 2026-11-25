package com.certifika2.chat.controller;

import com.certifika2.chat.model.EventoProcesado;
import com.certifika2.chat.repository.EventoProcesadoRepository;
import com.certifika2.chat.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat/admin/entregas")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class DeliveryAdminController {
    private final EventoProcesadoRepository processed;
    private final NotificacionRepository notifications;

    /** Bounded, read-only lookup; POST carries event IDs without a very long query string. */
    @PostMapping("/consulta")
    @Transactional(readOnly = true)
    public Report lookup(@RequestBody List<String> ids) {
        if (ids == null || ids.size() > 200 || ids.stream().anyMatch(id -> id == null || id.isBlank() || id.length() > 255)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requieren hasta 200 identificadores de evento válidos");
        }
        List<String> unique = ids.stream().distinct().toList();
        if (unique.isEmpty()) return new Report(Instant.now(), List.of());
        Set<String> confirmed = processed.findAllById(unique).stream().map(EventoProcesado::getId).collect(Collectors.toSet());
        Map<String, NotificacionRepository.DeliveryCount> counts = notifications.deliveryCounts(unique).stream()
                .collect(Collectors.toMap(NotificacionRepository.DeliveryCount::getEventId, Function.identity()));
        return new Report(Instant.now(), unique.stream().map(id -> {
            var count = counts.get(id);
            return new Receipt(id, confirmed.contains(id), count == null ? 0 : count.getDelivered(), count == null ? 0 : count.getReadCount());
        }).toList());
    }
    public record Receipt(String id, boolean processed, long delivered, long readCount) {}
    public record Report(Instant observedAt, List<Receipt> events) {}
}
