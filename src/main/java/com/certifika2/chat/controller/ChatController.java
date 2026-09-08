package com.certifika2.chat.controller;

import com.certifika2.chat.model.Mensaje;
import com.certifika2.chat.service.MensajeService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MensajeService mensajeService;

    // Recibe mensajes websocket dirigidos a /app/chat.curso.{idCurso}
    @MessageMapping("/chat.curso.{idCurso}")
    @SendTo("/topic/curso/{idCurso}")
    public Mensaje enviarMensaje(@DestinationVariable Long idCurso, 
                                 @Payload MensajeRequest request, 
                                 JwtAuthenticationToken authentication) {
        
        // Asumiendo que el Gateway reenvía el token JWT y Spring lo inyecta
        String idUsuario = authentication.getToken().getSubject();
        String nombre = authentication.getToken().getClaimAsString("name");
        
        // Guarda en base de datos y lo devuelve para ser transmitido a todos los subscritos
        return mensajeService.guardarMensaje(idCurso, idUsuario, nombre, request.getContenido());
    }

    // Endpoint REST para recuperar el historial de mensajes al entrar a un curso
    @GetMapping("/api/chat/historial/{idCurso}")
    @ResponseBody
    public List<Mensaje> obtenerHistorial(@PathVariable Long idCurso) {
        return mensajeService.obtenerHistorial(idCurso);
    }
}

@Data
class MensajeRequest {
    private String contenido;
}
