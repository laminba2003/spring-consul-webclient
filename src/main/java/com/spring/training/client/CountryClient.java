package com.spring.training.client;

import com.spring.training.exception.EntityNotFoundException;
import com.spring.training.model.Country;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class CountryClient {

    final WebClient client;

    public Flux<Country> getCountries() {
        return client.get().uri("/countries")
                .retrieve()
                .bodyToFlux(Country.class);
    }

    public Mono<Country> getCountry(String name) {
        return client.get().uri("/countries/{name}", name)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("country not found with name : " + name)))
                .bodyToMono(Country.class);
    }

    public Mono<Country> createCountry(Country country) {
        return client.post().uri("/countries")
                .body(Mono.just(country), Country.class)
                .retrieve()
                .bodyToMono(Country.class);
    }

    public Mono<Country> updateCountry(String name, Country country) {
        return client.put().uri("/countries/{name}", name)
                .body(Mono.just(country), Country.class)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> Mono.error(new EntityNotFoundException("country not found with name : " + name)))
                .bodyToMono(Country.class);
    }

    public Mono<Void> deleteCountry(String name) {
        return client.delete().uri("/countries/{name}", name)
                .retrieve()
                .bodyToMono(Void.class);
    }

}
