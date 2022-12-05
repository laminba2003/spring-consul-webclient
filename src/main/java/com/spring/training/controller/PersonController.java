package com.spring.training.controller;

import com.spring.training.model.Person;
import com.spring.training.service.PersonService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("persons")
@AllArgsConstructor
public class PersonController {

    final PersonService service;

    @GetMapping("{id}")
    public Mono<Person> getPerson(@PathVariable("id") Long id) {
        return service.getPerson(id);
    }

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Person> getPersons() {
        return service.getPersons();
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public Mono<Person> createPerson(@RequestBody Person person) {
        return service.createPerson(person);
    }

    @PutMapping("{id}")
    public Mono<Person> updatePerson(@PathVariable("id") Long id, @RequestBody Person person) {
        return service.updatePerson(id, person);
    }

    @DeleteMapping("{id}")
    public Mono<Void> deletePerson(@PathVariable("id") Long id) {
        return service.deletePerson(id);
    }

}