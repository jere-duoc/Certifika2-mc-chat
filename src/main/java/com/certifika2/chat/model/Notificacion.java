package com.certifika2.chat.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity @Getter @Setter
@Table(name = "notificaciones", uniqueConstraints = @UniqueConstraint(columnNames = {"eventoId", "usuarioId"}),
        indexes = @Index(name = "idx_notificacion_usuario", columnList = "usuarioId,fechaCreacion"))
public class Notificacion {
    @Id private String id;
    @Column(nullable = false) private String eventoId;
    @Column(nullable = false) private String usuarioId;
    private String codigoCurso;
    private String tipo;
    @Column(length = 180) private String titulo;
    @Column(length = 3000) private String contenido;
    @Column(nullable = false) private Instant fechaCreacion = Instant.now();
    @Column(nullable = false) private boolean leida;
}
