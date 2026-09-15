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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final MensajeService mensajeService;

    // Recibe mensajes websocket dirigidos a /app/chat.curso.{idCurso}
    @MessageMapping("/chat.curso.{idCurso}")
    @SendTo("/topic/curso/{idCurso}")
    public Mensaje enviarMensaje(@DestinationVariable String idCurso, 
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
    public List<Mensaje> obtenerHistorial(@PathVariable String idCurso) {
        return mensajeService.obtenerHistorial(idCurso);
    }

    @PostMapping("/api/chat/curso/{idCurso}/mensajes")
    @ResponseBody
    public ResponseEntity<Mensaje> enviarMensajeRest(
            @PathVariable String idCurso,
            @RequestBody MensajeRequest request,
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(name = "X-OIDC-Display-Name", required = false) String displayName) {
        String contenido = request.getContenido() == null ? "" : request.getContenido().trim();
        if (contenido.isBlank() || contenido.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El mensaje debe tener entre 1 y 2000 caracteres");
        }
        String nombre = firstNonBlank(
                displayName,
                jwt.getClaimAsString("name"),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("username"),
                "Usuario");
        return ResponseEntity.ok(mensajeService.guardarMensaje(
                idCurso, jwt.getSubject(), nombre, contenido));
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) return value;
        }
        return "Usuario";
    }
}

@Data
class MensajeRequest {
    private String contenido;
}
