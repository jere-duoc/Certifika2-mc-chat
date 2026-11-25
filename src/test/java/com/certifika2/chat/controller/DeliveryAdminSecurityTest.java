package com.certifika2.chat.controller;

import com.certifika2.chat.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeliveryAdminSecurityTest {
    @Configuration @EnableMethodSecurity
    static class Config {
        @Bean EventoProcesadoRepository processed() { return mock(EventoProcesadoRepository.class); }
        @Bean NotificacionRepository notifications() { return mock(NotificacionRepository.class); }
        @Bean DeliveryAdminController controller(EventoProcesadoRepository p, NotificacionRepository n) { return new DeliveryAdminController(p, n); }
    }
    @Test void studentCannotReadAdministrativeDeliveryData() {
        try (var context = new AnnotationConfigApplicationContext(Config.class)) {
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("student", "", List.of(new SimpleGrantedAuthority("ROLE_ESTUDIANTE"))));
            assertThatThrownBy(() -> context.getBean(DeliveryAdminController.class).lookup(List.of("private-event"))).isInstanceOf(AccessDeniedException.class);
            verifyNoInteractions(context.getBean(EventoProcesadoRepository.class), context.getBean(NotificacionRepository.class));
        } finally { SecurityContextHolder.clearContext(); }
    }
}
