package com.spring.training.config;

import com.spring.training.client.CountryClient;
import com.spring.training.client.PersonClient;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class ApplicationConfig {

    @Bean
    public WebClient webClient(ClientConfig config) {
        HttpClient httpClient = HttpClient.create()
                .doOnConnected(connection ->  connection
                .addHandlerLast(new ReadTimeoutHandler(10))
                .addHandlerLast(new WriteTimeoutHandler(10)));

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return webClientBuilder()
                .baseUrl(config.getUrl())
                .clientConnector(connector)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public PersonClient personClient(WebClient client) {
        return new PersonClient(client);
    }

    @Bean
    public CountryClient countryClient(WebClient client) {
        return new CountryClient(client);
    }

    @Bean
    public WebProperties.Resources resources() {
        return new WebProperties.Resources();
    }

    @Bean
    @ConfigurationProperties(prefix = "remote.services")
    public ClientConfig clientConfig() {
        return new ClientConfig();
    }
}
