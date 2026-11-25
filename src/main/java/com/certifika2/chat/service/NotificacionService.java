package com.certifika2.chat.service;

import com.certifika2.chat.dto.AnuncioEvent;
import com.certifika2.chat.model.EventoProcesado;
import com.certifika2.chat.model.Notificacion;
import com.certifika2.chat.repository.EventoProcesadoRepository;
import com.certifika2.chat.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.UUID;

@Service @RequiredArgsConstructor
public class NotificacionService {
    private final EventoProcesadoRepository procesados;
    private final NotificacionRepository notificaciones;
    private final MensajeService mensajes;

    @Transactional
    public boolean recibir(AnuncioEvent evento, String messageId) {
        String id = evento.eventoId() == null || evento.eventoId().isBlank()
                ? "legacy-" + messageId : evento.eventoId();
        if (procesados.existsById(id)) return false;
        // Flush before effects: concurrent duplicate delivery rolls back, then its retry sees the marker.
        procesados.saveAndFlush(new EventoProcesado(id));
        String sala = evento.codigoCurso() != null ? evento.codigoCurso() : String.valueOf(evento.idCurso());
        String texto = "📌 " + evento.titulo() + "\n\n" + evento.contenido();
        mensajes.guardarMensaje(sala, evento.idProfesor(), "📢 " +
                (evento.nombreProfesor() == null ? "Profesor" : evento.nombreProfesor()),
                texto.substring(0, Math.min(2000, texto.length())));
        if (evento.destinatarios() != null) {
            evento.destinatarios().stream().filter(u -> u != null && !u.isBlank()).distinct().forEach(usuario -> {
                Notificacion n = new Notificacion();
                n.setId(UUID.nameUUIDFromBytes((id + ":" + usuario).getBytes(java.nio.charset.StandardCharsets.UTF_8)).toString());
                n.setEventoId(id);
                n.setUsuarioId(usuario);
                n.setCodigoCurso(sala);
                n.setTipo(evento.tipoEvento() == null ? "ANUNCIO" : evento.tipoEvento());
                n.setTitulo(evento.titulo());
                n.setContenido(evento.contenido());
                notificaciones.save(n);
            });
        }
        return true;
    }

    @Transactional(readOnly = true)
    public Bandeja listar(String usuario) {
        return new Bandeja(notificaciones.findTop50ByUsuarioIdOrderByFechaCreacionDesc(usuario).stream()
                .map(Vista::de).toList(), notificaciones.countByUsuarioIdAndLeidaFalse(usuario));
    }

    @Transactional
    public Vista marcarLeida(String id, String usuario) {
        Notificacion n = notificaciones.findByIdAndUsuarioId(id, usuario)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notificación no encontrada"));
        n.setLeida(true);
        return Vista.de(n);
    }

    public record Bandeja(List<Vista> items, long noLeidas) {}
    public record Vista(String id, String codigoCurso, String tipo, String titulo, String contenido,
                        java.time.Instant fechaCreacion, boolean leida) {
        static Vista de(Notificacion n) { return new Vista(n.getId(), n.getCodigoCurso(), n.getTipo(),
                n.getTitulo(), n.getContenido(), n.getFechaCreacion(), n.isLeida()); }
    }
}
