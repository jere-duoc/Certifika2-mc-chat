package com.certifika2.chat.controller;
import com.certifika2.chat.service.NotificacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/chat/notificaciones") @RequiredArgsConstructor
public class NotificacionController {
    private final NotificacionService service;
    @GetMapping public NotificacionService.Bandeja listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(jwt.getSubject());
    }
    @RequestMapping(value = "/{id}/leida", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public NotificacionService.Vista leida(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return service.marcarLeida(id, jwt.getSubject());
    }
}
