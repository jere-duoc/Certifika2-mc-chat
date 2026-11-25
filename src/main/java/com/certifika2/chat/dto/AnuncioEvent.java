package com.certifika2.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

@com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
public record AnuncioEvent(
        Long idCurso,
        String codigoCurso,
        String titulo,
        String contenido,
        String idProfesor,
        String nombreProfesor,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime fechaCreacion,
        String eventoId,
        String tipoEvento,
        java.util.List<String> destinatarios
) {}
