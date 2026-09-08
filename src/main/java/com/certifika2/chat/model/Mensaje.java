package com.certifika2.chat.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Entity
@Table(name = "mensajes")
@Data
@NoArgsConstructor
public class Mensaje {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idMensaje;

    private Long idCurso; // Sala de chat por curso
    private String idUsuarioEmisor; // Guardamos ID de Google/Cognito o email
    private String nombreEmisor;
    
    @Column(length = 2000)
    private String contenido;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio;
}
