package com.spring.training.controller;

import com.spring.training.model.Country;
import com.spring.training.service.CountryService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("countries")
@AllArgsConstructor
public class CountryController {

    final CountryService service;

    @GetMapping("{name}")
    public Mono<Country> getCountry(@PathVariable("name") String name) {
        return service.getCountry(name);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Country> getCountries() {
        return service.getCountries();
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Country> createCountry(@RequestBody Country country) {
        return service.createCountry(country);
    }

    @PutMapping("{name}")
    public Mono<Country> updateCountry(@PathVariable("name") String name, @RequestBody Country user) {
        return service.updateCountry(name, user);
    }

    @DeleteMapping("{name}")
    public Mono<Void> deleteCountry(@PathVariable("name") String name) {
        return service.deleteCountry(name);
    }

}