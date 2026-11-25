package com.certifika2.chat.controller;
import com.certifika2.chat.service.NotificacionService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class NotificacionControllerTest {
    @Test void putReadUsesAuthenticatedSubjectNotCallerSuppliedOwner() throws Exception {
        NotificacionService service = mock(NotificacionService.class);
        NotificacionController controller = new NotificacionController(service);
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "none").subject("authenticated-student").build();
        controller.leida("notification-id", jwt);
        verify(service).marcarLeida("notification-id", "authenticated-student");
        RequestMapping mapping = NotificacionController.class.getMethod("leida", String.class, Jwt.class)
                .getAnnotation(RequestMapping.class);
        assertThat(mapping.method()).contains(RequestMethod.PUT);
    }
}
