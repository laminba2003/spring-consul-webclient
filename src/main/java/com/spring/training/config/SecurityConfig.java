package com.spring.training.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**", "/v3/api-docs/**", "/swagger-ui.html", "/webjars/**", "/swagger-ui/**").permitAll()
                .pathMatchers("/**").authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt().jwtAuthenticationConverter(new JwtConverter()))
                .cors().and().csrf().disable();
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern(CorsConfiguration.ALL);
        config.setAllowedHeaders(Collections.singletonList(CorsConfiguration.ALL));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PUT", "OPTIONS", "PATCH", "DELETE"));
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}