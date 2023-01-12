package com.spring.training.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

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
    @ConfigurationProperties("cors")
    public Cors cors() {
        return new Cors();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(Cors cors) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(cors.isAllowCredentials());
        config.addAllowedOriginPattern(cors.getAllowedOriginPattern());
        config.setAllowedHeaders(cors.getAllowedHeaders());
        config.setAllowedMethods(cors.getAllowedMethods());
        source.registerCorsConfiguration("/**", config);
        return source;
    }


}