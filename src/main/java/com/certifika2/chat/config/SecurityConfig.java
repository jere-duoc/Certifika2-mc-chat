package com.certifika2.chat.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt
                        .decoder(jwtDecoder)
                        .jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuer,
            @Value("${cognito.client-id}") String clientId) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(issuer.replaceAll("/$", "") + "/.well-known/jwks.json")
                .build();
        OAuth2TokenValidator<Jwt> defaults = JwtValidators.createDefaultWithIssuer(issuer);
        OAuth2TokenValidator<Jwt> tokenUse = jwt -> validate(
                "access".equals(jwt.getClaimAsString("token_use")),
                "invalid_token_use", "Se requiere un access token de Cognito");
        OAuth2TokenValidator<Jwt> appClient = jwt -> validate(
                clientId.equals(jwt.getClaimAsString("client_id")),
                "invalid_client_id", "El token fue emitido para otro app client");
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaults, tokenUse, appClient));
        return decoder;
    }

    @Bean
    Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter scopeConverter = new JwtGrantedAuthoritiesConverter();
        scopeConverter.setAuthorityPrefix("SCOPE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            Collection<GrantedAuthority> scopes = scopeConverter.convert(jwt);
            if (scopes != null) authorities.addAll(scopes);
            List<String> groups = jwt.getClaimAsStringList("cognito:groups");
            if (groups != null) groups.stream()
                    .map(group -> group.startsWith("ROLE_") ? group : "ROLE_" + group)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
            return authorities;
        });
        return converter;
    }

    private OAuth2TokenValidatorResult validate(boolean valid, String code, String description) {
        return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(new OAuth2Error(code, description, null));
    }
}
