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

    @Column(nullable = false, length = 120)
    private String idCurso; // Código estable de la sala/curso
    private String idUsuarioEmisor; // `sub` estable del usuario en Cognito
    private String nombreEmisor;
    
    @Column(length = 2000)
    private String contenido;

    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEnvio;
}
