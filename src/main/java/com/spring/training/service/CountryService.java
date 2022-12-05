package com.spring.training.service;

import com.spring.training.client.CountryClient;
import com.spring.training.model.Country;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@AllArgsConstructor
public class CountryService {

    final CountryClient client;

    public Flux<Country> getCountries() {
        return client.getCountries();
    }

    public Mono<Country> getCountry(String name) {
        return client.getCountry(name);
    }

    public Mono<Country> createCountry(Country country) {
        return client.createCountry(country);
    }

    public Mono<Country> updateCountry(String name, Country country) {
        return client.updateCountry(name, country);
    }

    public Mono<Void> deleteCountry(String name) {
        return client.deleteCountry(name);
    }


}
