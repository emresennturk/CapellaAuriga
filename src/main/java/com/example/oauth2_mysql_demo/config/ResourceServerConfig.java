package com.example.oauth2_mysql_demo.config;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class ResourceServerConfig {

    @Autowired
    private JWKSource<SecurityContext> jwkSource;

    @Bean
    public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/hello", "/api/deposit", "/api/request-withdrawal").authenticated() // Sadece kimlik doğrulama gerektirir
                        .requestMatchers("/api/**").hasAuthority("SCOPE_read") // Diğer API'ler token gerektirir
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)))
                );
        return http.build();
    }
}
